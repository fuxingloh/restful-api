package munch.restful.topic;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.MessageAttributeValue;
import com.amazonaws.services.sns.model.PublishRequest;
import munch.restful.core.JsonUtils;

/**
 * Created by: Fuxing
 * Date: 2019-03-01
 * Time: 21:57
 * Project: restful-api
 */
public class RestfulTopic<T> {

    protected final AmazonSNS amazonSNS;
    protected final String topicArn;

    public RestfulTopic(AmazonSNS amazonSNS, String topicArn) {
        this.amazonSNS = amazonSNS;
        this.topicArn = topicArn;
    }

    /**
     * @param body to publish
     */
    public void publish(T body) {
        PublishRequest request = new PublishRequest(topicArn, JsonUtils.toString(body));
        beforePublish(request, body);

        amazonSNS.publish(request);
    }

    /**
     * @param request to inject before publish
     * @param body    to publish
     */
    protected void beforePublish(PublishRequest request, T body) {
    }


    public static MessageAttributeValue withString(String value) {
        return new MessageAttributeValue()
                .withDataType("String")
                .withStringValue(value);
    }
}
