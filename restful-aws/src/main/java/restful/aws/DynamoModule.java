package restful.aws;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import munch.restful.WaitFor;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Duration;

/**
 * Created by: Fuxing
 * Date: 25/7/18
 * Time: 12:22 AM
 * Project: catalyst
 */
public final class DynamoModule extends AbstractModule {

    @Override
    protected void configure() {
        Config config = ConfigFactory.load().getConfig("services.aws");

        if (config.hasPath("dynamodb.url")) {
            requestInjection(this);
        }
    }

    @Inject
    void setup() throws InterruptedException {
        if (!ConfigFactory.load().hasPath("services.localstack.dashboard.url")) return;
        WaitFor.localstack(ConfigFactory.load().getString("services.localstack.dashboard.url"), Duration.ofSeconds(120));
    }

    @Provides
    @Singleton
    AmazonDynamoDB provideAmazonDynamoDB() {
        Config config = ConfigFactory.load().getConfig("services.aws.dynamodb");
        if (config.hasPath("url")) {
            return AmazonDynamoDBClientBuilder.standard()
                    .withEndpointConfiguration(
                            new AwsClientBuilder.EndpointConfiguration(
                                    config.getString("url"), "us-west-2"))
                    .withCredentials(
                            new AWSStaticCredentialsProvider(
                                    new BasicAWSCredentials("foo", "bar")))
                    .build();
        } else {
            return AmazonDynamoDBClientBuilder.defaultClient();
        }
    }

    @Provides
    @Singleton
    DynamoDB provideDynamoDB(AmazonDynamoDB amazonDynamoDB) {
        return new DynamoDB(amazonDynamoDB);
    }
}
