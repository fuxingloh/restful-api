package munch.restful.topic;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.*;
import munch.restful.core.JsonUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Created by: Fuxing
 * Date: 25/10/18
 * Time: 7:45 PM
 * Project: catalyst
 */
public class RestfulQueue<T> {
    protected final AmazonSQS amazonSQS;
    protected final String url;
    protected final Class<T> clazz;

    public RestfulQueue(AmazonSQS amazonSQS, String url, Class<T> clazz) {
        this.amazonSQS = amazonSQS;
        this.url = url;
        this.clazz = clazz;
    }

    /**
     * @param body to send to queue
     */
    public void queue(T body) {
        SendMessageRequest request = new SendMessageRequest();
        request.setQueueUrl(url);
        request.setMessageBody(JsonUtils.toString(body));
        amazonSQS.sendMessage(request);
    }

    /**
     * @param threads  to run with for parallel processing
     * @param consumer to consume message
     * @return whether any message is consumed
     */
    public boolean consume(int threads, Consumer<T> consumer) {
        List<Message> messages = getMessages();
        if (messages.isEmpty()) return false;

        ExecutorService service = Executors.newFixedThreadPool(threads);
        List<DeleteMessageBatchRequestEntry> deletes = new ArrayList<>();

        try {
            CompletableFuture[] futures = messages.stream()
                    .map(message -> mapFuture(service, message, consumer, deletes))
                    .toArray(CompletableFuture[]::new);

            CompletableFuture.allOf(futures).join();
            return true;
        } finally {
            service.shutdown();

            // Delete completed entry
            if (!deletes.isEmpty()) {
                amazonSQS.deleteMessageBatch(url, deletes);
            }
        }
    }

    protected List<Message> getMessages() {
        ReceiveMessageResult result = amazonSQS.receiveMessage(
                new ReceiveMessageRequest(url)
                        .withMaxNumberOfMessages(10)
        );

        return result.getMessages();
    }

    protected CompletableFuture mapFuture(ExecutorService service, Message message, Consumer<T> consumer, List<DeleteMessageBatchRequestEntry> deletes) {
        Runnable runnable = () -> {
            consumer.accept(JsonUtils.toObject(message.getBody(), clazz));
            deletes.add(new DeleteMessageBatchRequestEntry(message.getMessageId(), message.getReceiptHandle()));
        };

        return CompletableFuture.runAsync(runnable, service);
    }
}
