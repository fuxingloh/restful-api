package munch.restful.server.dynamodb;

import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.api.QueryApi;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.fasterxml.jackson.databind.JsonNode;
import munch.restful.core.exception.ParamException;
import munch.restful.server.JsonCall;

import javax.annotation.Nullable;

/**
 * Created by: Fuxing
 * Date: 3/5/18
 * Time: 4:01 PM
 * Project: restful-api
 */
public abstract class RestfulDynamoHashRangeService<T> extends RestfulDynamoService<T> {

    protected final String rangeName;

    /**
     * @param table     dynamodb table
     * @param clazz     of data
     * @param hashName  name of hash
     * @param rangeName name of range
     */
    protected RestfulDynamoHashRangeService(Table table, Class<T> clazz, String hashName, String rangeName) {
        this(table, clazz, hashName, rangeName, 100);
    }

    /**
     * @param table     dynamodb table
     * @param clazz     of data
     * @param hashName  range of hash
     * @param rangeName range of range
     * @param maxSize   max size per query
     */
    protected RestfulDynamoHashRangeService(Table table, Class<T> clazz, String hashName, String rangeName, int maxSize) {
        super(table, clazz, hashName, maxSize);
        this.rangeName = rangeName;
    }

    /**
     * @param call json call with pathString(hashName), queryString(rangeName, null), and queryInt(size, 20)
     * @return JsonNode result to return
     * @see RestfulDynamoHashRangeService#list(JsonCall)
     */
    protected JsonNode list(JsonCall call) {
        return list(table,
                call.pathString(hashName),
                call.queryString("next." + rangeName, null),
                call.queryInt("size", 20));
    }

    /**
     * @param queryApi  to query
     * @param hash      value
     * @param nextRange to start from
     * @param size      per list
     * @return JsonNode result to return
     * @see QueryApi#query(QuerySpec)
     * @see QuerySpec#withRangeKeyCondition(RangeKeyCondition)
     */
    protected JsonNode list(QueryApi queryApi, Object hash, @Nullable Object nextRange, int size) {
        return list(queryApi, hashName, hash, rangeName, nextRange, size);
    }

    /**
     * @param call json call with pathString(hashName) and pathString(rangeName)
     * @return Object or nullable
     * @see RestfulDynamoHashRangeService#get(Object, Object)
     */
    @Nullable
    protected T get(JsonCall call) {
        return get(call.pathString(hashName), call.pathString(rangeName));
    }

    /**
     * @param hash  value
     * @param range value
     * @return Object or nullable
     */
    @Nullable
    protected T get(Object hash, Object range) {
        ParamException.requireNonNull(hashName, hash);
        ParamException.requireNonNull(rangeName, range);

        Item item = table.getItem(hashName, hash, rangeName, range);
        return toData(item);
    }

    /**
     * @param call json call with pathString(hashName), pathString(rangeName), and request body
     * @return Object saved
     * @see RestfulDynamoHashRangeService#put(Object, Object, JsonNode)
     */
    protected T put(JsonCall call) {
        return put(call.pathString(hashName), call.pathString(rangeName), call.bodyAsJson());
    }

    /**
     * @param hash  value
     * @param range value
     * @param json  body
     * @return Object saved
     */
    protected T put(Object hash, Object range, JsonNode json) {
        ParamException.requireNonNull(hashName, hash);
        ParamException.requireNonNull(rangeName, range);

        Item item = toItem(json, hash);
        item.with(rangeName, range);

        PutItemOutcome outcome = table.putItem(item);
        return toData(outcome.getItem());
    }

    /**
     * @param call json call with pathString(hashName) and pathString(rangeName)
     * @return Deleted Data or null if not found
     * @see RestfulDynamoHashRangeService#delete(Object, Object)
     */
    @Nullable
    protected T delete(JsonCall call) {
        return delete(call.pathString(hashName), call.pathString(rangeName));
    }

    /**
     * @param hash  value
     * @param range value
     * @return Deleted Data or null if not found
     */
    @Nullable
    protected T delete(Object hash, Object range) {
        ParamException.requireNonNull(hashName, hash);
        ParamException.requireNonNull(rangeName, range);

        DeleteItemOutcome outcome = table.deleteItem(hashName, hash, rangeName, range);
        return toData(outcome.getItem());
    }
}
