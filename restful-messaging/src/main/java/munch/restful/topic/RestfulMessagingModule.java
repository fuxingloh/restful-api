package munch.restful.topic;

import com.google.inject.AbstractModule;
import restful.aws.SNSModule;
import restful.aws.SQSModule;

/**
 * Created by: Fuxing
 * Date: 2019-03-02
 * Time: 20:31
 * Project: restful-api
 */
public class RestfulMessagingModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new SQSModule());
        install(new SNSModule());
    }

}
