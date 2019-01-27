package munch.restful.server.dynamodb;

import com.amazonaws.services.dynamodbv2.document.DeleteItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.KeyAttribute;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;
import com.fasterxml.jackson.databind.JsonNode;
import munch.restful.core.NextNodeList;
import munch.restful.core.exception.ParamException;
import munch.restful.server.JsonCall;
import munch.restful.server.JsonResult;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by: Fuxing
 * Date: 3/5/18
 * Time: 4:01 PM
 * Project: restful-api
 */
public abstract class RestfulDynamoHashService<T> extends RestfulDynamoService<T> {

    /**
     * @param table    dynamodb table
     * @param clazz    of data
     * @param hashName range of hash
     * @param maxSize  max size per query
     */
    protected RestfulDynamoHashService(Table table, Class<T> clazz, String hashName, int maxSize) {
        super(table, clazz, hashName, maxSize);
    }

    /**
     * @param table    dynamodb table
     * @param clazz    of data
     * @param hashName range of hash
     */
    protected RestfulDynamoHashService(Table table, Class<T> clazz, String hashName) {
        super(table, clazz, hashName, 100);
    }

    /**
     * @param call json call with queryString(hashName)
     * @return JsonNode result
     * @see RestfulDynamoHashService#list(Object, int)
     */
    protected NextNodeList<T> list(JsonCall call) {
        String hash = call.queryString("next." + hashName, null);
        return list(hash, querySize(call));
    }

    /**
     * @param nextHash hash value
     * @param size     size per list
     * @return JsonNode result to return
     * @see Table#query(QuerySpec)
     * @see QuerySpec#withExclusiveStartKey(KeyAttribute...)
     */
    protected NextNodeList<T> list(Object nextHash, int size) {
        ScanSpec scanSpec = new ScanSpec();
        scanSpec.withMaxResultSize(size);

        if (nextHash != null) {
            scanSpec.withExclusiveStartKey(hashName, nextHash);
        }

        List<T> dataList = new ArrayList<>();
        Item lastItem = null;
        for (Item item : table.scan(scanSpec)) {
            lastItem = item;
            dataList.add(toData(item));
        }

        if (lastItem == null || dataList.size() != size) {
            // If no more next
            return new NextNodeList<>(dataList);
        } else {
            // Have next, send next object
            return new NextNodeList<>(dataList, hashName, lastItem.get(hashName));
        }
    }

    /**
     * @param call json call with pathString(hashName)
     * @return Object or nullable
     * @see RestfulDynamoHashService#get(Object)
     */
    @Nullable
    protected T get(JsonCall call) {
        return get(call.pathString(hashName));
    }

    /**
     * @param hash value
     * @return Object or nullable
     */
    @Nullable
    protected T get(Object hash) {
        ParamException.requireNonNull(hashName, hash);

        Item item = table.getItem(hashName, hash);
        return toData(item);
    }

    /**
     * @param call json call with pathString(hashName) and request body
     * @return Meta200 if successful
     * @see RestfulDynamoHashService#put(Object, JsonNode)
     */
    protected JsonResult put(JsonCall call) {
        return put(call.pathString(hashName), call.bodyAsJson());
    }

    /**
     * @param hash value
     * @param json body
     * @return Meta200 if successful
     */
    protected JsonResult put(Object hash, JsonNode json) {
        ParamException.requireNonNull(hashName, hash);

        Item item = toItem(json, hash);
        table.putItem(item);
        return result(200);
    }

    /**
     * @param call json call with pathString(hashName)
     * @return Deleted Data or null if not found
     * @see RestfulDynamoHashService#delete(Object)
     */
    @Nullable
    protected T delete(JsonCall call) {
        return delete(call.pathString(hashName));
    }

    /**
     * @param hash value
     * @return Deleted Data or null if not found
     */
    @Nullable
    protected T delete(Object hash) {
        ParamException.requireNonNull(hashName, hash);

        DeleteItemOutcome outcome = table.deleteItem(new DeleteItemSpec()
                .withPrimaryKey(hashName, hash)
                .withReturnValues(ReturnValue.ALL_OLD));
        return toData(outcome.getItem());
    }
}
