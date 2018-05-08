package munch.restful.server.dynamodb;

import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import munch.restful.core.exception.ParamException;
import munch.restful.server.JsonCall;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
     * @param call json call with queryString(hashName), queryString("size")
     * @return JsonNode result
     * @see RestfulDynamoHashService#list(Object, int)
     */
    protected JsonNode list(JsonCall call) {
        String hash = call.queryString("next." + hashName, null);
        int size = resolveSize(call.queryInt("size", 20));
        return list(hash, size);
    }

    /**
     * @param nextHash hash value
     * @param size size per list
     * @return JsonNode result to return
     * @see Table#query(QuerySpec)
     * @see QuerySpec#withExclusiveStartKey(KeyAttribute...)
     */
    protected JsonNode list(Object nextHash, int size) {
        ScanSpec scanSpec = new ScanSpec();
        scanSpec.withMaxPageSize(resolveSize(size));

        if (nextHash != null) {
            scanSpec.withExclusiveStartKey(hashName, nextHash);
        }

        List<Item> items = new ArrayList<>();
        table.scan(scanSpec).forEach(items::add);


        List<T> dataList = items.stream().map(this::toData).collect(Collectors.toList());
        ObjectNode node = nodes(200, dataList);

        // If no more next
        if (items.size() != size) return node;

        // Have next, send next object
        ObjectNode next = node.putObject("next");
        next.putPOJO(hashName, items.get(size - 1).get(hashName));
        return node;
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
     * @return Object saved
     * @see RestfulDynamoHashService#put(Object, JsonNode)
     */
    protected T put(JsonCall call) {
        return put(call.pathString(hashName), call.bodyAsJson());
    }

    /**
     * @param hash value
     * @param json body
     * @return Object saved
     */
    protected T put(Object hash, JsonNode json) {
        ParamException.requireNonNull(hashName, hash);

        Item item = toItem(json, hash);
        PutItemOutcome outcome = table.putItem(item);
        return toData(outcome.getItem());
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

        DeleteItemOutcome outcome = table.deleteItem(hashName, hash);
        return toData(outcome.getItem());
    }
}
