package munch.restful.server.dynamodb;

import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.api.QueryApi;
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import munch.restful.core.JsonUtils;
import munch.restful.core.NextNodeList;
import munch.restful.core.exception.ParamException;
import munch.restful.server.JsonCall;
import munch.restful.server.JsonResult;

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
    protected NextNodeList<T> list(JsonCall call) {
        return list(table,
                call.pathString(hashName),
                call.queryString("next." + rangeName, null),
                querySize(call));
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
    protected NextNodeList<T> list(QueryApi queryApi, Object hash, @Nullable Object nextRange, int size) {
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
    protected JsonResult put(JsonCall call) {
        return put(call.pathString(hashName), call.pathString(rangeName), call.bodyAsJson());
    }

    /**
     * @param hash  value
     * @param range value
     * @param json  body
     * @return Object saved
     */
    protected JsonResult put(Object hash, Object range, JsonNode json) {
        ParamException.requireNonNull(hashName, hash);
        ParamException.requireNonNull(rangeName, range);

        ((ObjectNode) json).putPOJO(rangeName, range);
        Item item = toItem(json, hash);

        table.putItem(item);
        return JsonResult.ok();
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

        DeleteItemOutcome outcome = table.deleteItem(new DeleteItemSpec()
                .withPrimaryKey(hashName, hash, rangeName, range)
                .withReturnValues(ReturnValue.ALL_OLD));
        return toData(outcome.getItem());
    }

    @Override
    protected T patch(T object, JsonNode body, String... fields) {
        ObjectNode current = JsonUtils.toTree(object);
        for (String field : fields) {
            if (field.equals(hashName)) continue;
            if (field.equals(rangeName)) continue;
            if (!body.has(field)) continue;

            current.set(field, body.path(field));
        }

        return JsonUtils.toObject(current, clazz);
    }
}
