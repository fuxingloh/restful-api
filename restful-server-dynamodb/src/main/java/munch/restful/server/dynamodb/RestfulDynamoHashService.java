package munch.restful.server.dynamodb;

import com.amazonaws.services.dynamodbv2.document.DeleteItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
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
public abstract class RestfulDynamoHashService<T> extends RestfulDynamoService<T> {

    /**
     * @param table    dynamodb table
     * @param clazz    of data
     * @param hashName range of hash
     */
    protected RestfulDynamoHashService(Table table, Class<T> clazz, String hashName) {
        super(table, clazz, hashName);
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
