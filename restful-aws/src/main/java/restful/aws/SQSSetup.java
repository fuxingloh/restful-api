package restful.aws;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.AmazonSQSException;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.amazonaws.services.sqs.model.QueueDoesNotExistException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Created by: Fuxing
 * Date: 25/7/18
 * Time: 1:03 AM
 * Project: catalyst
 */
public abstract class SQSSetup {
    protected static final Logger logger = LoggerFactory.getLogger(SQSSetup.class);
    protected static final Map<String, String> DEFAULT_ATTRIBUTES = Map.of(
            "DelaySeconds", "90", // 90 seconds before processed
            "MessageRetentionPeriod", "1209600", // Message retention is 14 days
            "VisibilityTimeout", "1800" // 30 minutes visibility timeout for message in flight
    );

    protected final AmazonSQS amazonSQS;

    protected SQSSetup(AmazonSQS amazonSQS) {
        this.amazonSQS = amazonSQS;
    }

    public void create(String queueName) {
        try {
            GetQueueUrlResult result = amazonSQS.getQueueUrl(queueName);
            logger.info("Found Queue: url: {}", result.getQueueUrl());
        } catch (QueueDoesNotExistException e) {
            try {
                amazonSQS.createQueue(new CreateQueueRequest(queueName)
                        .withAttributes(DEFAULT_ATTRIBUTES)
                );
            } catch (AmazonSQSException exception) {
                if (exception.getMessage().contains("Queue already exists")) return;
                throw exception;
            }
        }
    }
}
