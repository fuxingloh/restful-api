package munch.restful.topic;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sqs.AmazonSQS;
import restful.aws.SNSArnProvider;
import restful.aws.SQSUrlProvider;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import java.util.function.BiConsumer;

/**
 * Created by: Fuxing
 * Date: 2019-03-02
 * Time: 20:18
 * Project: restful-api
 */
@Singleton
public final class RestfulMessaging {

    private final SNSArnProvider arnProvider;
    private final SQSUrlProvider queueProvider;

    private final AmazonSNS amazonSNS;
    private final AmazonSQS amazonSQS;

    @Inject
    public RestfulMessaging(SNSArnProvider arnProvider, SQSUrlProvider queueProvider, AmazonSNS amazonSNS, AmazonSQS amazonSQS) {
        this.arnProvider = arnProvider;
        this.queueProvider = queueProvider;
        this.amazonSNS = amazonSNS;
        this.amazonSQS = amazonSQS;
    }

    @NotNull
    public <T> RestfulTopic<T> getTopic(String name) {
        String arn = arnProvider.get(name);
        return new RestfulTopic<>(amazonSNS, arn);
    }

    public <T> RestfulTopic<T> getTopic(String name, BiConsumer<PublishRequest, T> consumer) {
        String arn = arnProvider.get(name);
        return new RestfulTopic<>(amazonSNS, arn) {
            @Override
            protected void beforePublish(PublishRequest request, T body) {
                consumer.accept(request, body);
            }
        };
    }

    @NotNull
    public <T> RestfulQueue<T> getQueue(String name, Class<T> clazz) {
        String url = queueProvider.get(name);
        return new RestfulQueue<>(amazonSQS, url, clazz);
    }
}
