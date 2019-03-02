package restful.aws;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.*;
import com.amazonaws.services.dynamodbv2.util.TableUtils;

import java.util.function.Consumer;

/**
 * Created by: Fuxing
 * Date: 2019-01-29
 * Time: 16:36
 * Project: munch-user
 */
public abstract class DynamoSetup {
    public final AmazonDynamoDB amazonDynamoDB;
    public static final ProvisionedThroughput THROUGHPUT = new ProvisionedThroughput().withReadCapacityUnits(1000L).withWriteCapacityUnits(1000L);

    protected DynamoSetup(AmazonDynamoDB amazonDynamoDB) {
        this.amazonDynamoDB = amazonDynamoDB;
    }

    public AttributeDefinition attributeS(String name) {
        return new AttributeDefinition().withAttributeName(name).withAttributeType(ScalarAttributeType.S);
    }

    public AttributeDefinition attributeN(String name) {
        return new AttributeDefinition().withAttributeName(name).withAttributeType(ScalarAttributeType.N);
    }

    public LocalSecondaryIndex withLSI(String name, String hash, String range) {
        return new LocalSecondaryIndex()
                .withIndexName(name)
                .withKeySchema(
                        new KeySchemaElement().withAttributeName(hash).withKeyType(KeyType.HASH),
                        new KeySchemaElement().withAttributeName(range).withKeyType(KeyType.RANGE)
                )
                .withProjection(
                        new Projection().withProjectionType(ProjectionType.ALL)
                );
    }

    public GlobalSecondaryIndex withGSI(String name, String hash) {
        return new GlobalSecondaryIndex()
                .withIndexName(name)
                .withProvisionedThroughput(THROUGHPUT)
                .withKeySchema(
                        new KeySchemaElement().withAttributeName(hash).withKeyType(KeyType.HASH)
                )
                .withProjection(
                        new Projection().withProjectionType(ProjectionType.ALL)
                );
    }

    public GlobalSecondaryIndex withGSI(String name, String hash, String range) {
        return new GlobalSecondaryIndex()
                .withIndexName(name)
                .withProvisionedThroughput(THROUGHPUT)
                .withKeySchema(
                        new KeySchemaElement().withAttributeName(hash).withKeyType(KeyType.HASH),
                        new KeySchemaElement().withAttributeName(range).withKeyType(KeyType.RANGE)
                )
                .withProjection(
                        new Projection().withProjectionType(ProjectionType.ALL)
                );
    }

    public void create(String tableName, Consumer<CreateTableRequest> consumer) throws InterruptedException {
        CreateTableRequest request = new CreateTableRequest()
                .withTableName(tableName)
                .withProvisionedThroughput(THROUGHPUT);

        consumer.accept(request);

        TableUtils.createTableIfNotExists(amazonDynamoDB, request);
        TableUtils.waitUntilActive(amazonDynamoDB, tableName);
    }
}
