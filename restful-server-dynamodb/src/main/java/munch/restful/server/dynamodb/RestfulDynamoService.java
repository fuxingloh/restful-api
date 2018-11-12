package munch.restful.server.dynamodb;

import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.api.QueryApi;
import com.amazonaws.services.dynamodbv2.document.internal.ItemValueConformer;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import munch.restful.core.JsonUtils;
import munch.restful.core.exception.ParamException;
import munch.restful.core.exception.ValidationException;
import munch.restful.server.JsonCall;
import munch.restful.server.JsonResult;
import munch.restful.server.JsonService;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Created by: Fuxing
 * Date: 2/5/18
 * Time: 7:08 PM
 * Project: munch-partners
 */
public abstract class RestfulDynamoService<T> implements JsonService {
    protected static final ItemValueConformer valueConformer = new ItemValueConformer();

    protected final Table table;
    protected final Class<T> clazz;

    protected final String hashName;
    protected final int maxSize;

    /**
     * @param table    dynamodb table
     * @param clazz    of data
     * @param hashName range of hash
     * @param maxSize  max size per query
     */
    protected RestfulDynamoService(Table table, Class<T> clazz, String hashName, int maxSize) {
        this.table = table;
        this.clazz = clazz;
        this.hashName = hashName;
        this.maxSize = maxSize;
    }

    /**
     * This method will use hashName to get the hash value from call and rangeName to get the range value from call
     *
     * @param queryApi  to query
     * @param hashName  name of hash key
     * @param rangeName name of range key
     * @param call      json call
     * @return JsonNode result to return
     */
    protected JsonResult list(QueryApi queryApi, String hashName, String rangeName, JsonCall call) {
        return list(queryApi,
                hashName, call.pathString(hashName),
                rangeName, call.queryString("next." + rangeName, null),
                call.queryInt("size", 20)
        );
    }

    /**
     * @param queryApi  to query
     * @param hashName  name of hash
     * @param hash      value
     * @param rangeName name of range
     * @param nextRange to start from
     * @param size      per list
     * @return JsonNode result to return
     * @see QueryApi#query(QuerySpec)
     */
    protected JsonResult list(QueryApi queryApi, String hashName, Object hash, String rangeName, @Nullable Object nextRange, int size) {
        ParamException.requireNonNull(hashName, hash);

        QuerySpec querySpec = new QuerySpec();
        querySpec.withScanIndexForward(false);
        querySpec.withHashKey(hashName, hash);
        querySpec.withMaxResultSize(resolveSize(size));

        if (nextRange != null) {
            querySpec.withRangeKeyCondition(new RangeKeyCondition(rangeName).lt(nextRange));
        }

        List<T> dataList = new ArrayList<>();
        Item lastItem = null;
        for (Item item : queryApi.query(querySpec)) {
            lastItem = item;
            dataList.add(toData(item));
        }

        // If no more next
        JsonResult result = result(200, dataList);
        if (lastItem == null || dataList.size() != size) return result;

        // Have next, send next object
        result.put("next", Map.of(rangeName, lastItem.get(rangeName)));
        return result;
    }

    /**
     * This method will also validate the data
     * Hash & Range will also be validated if present
     * Basically: if a not-null constraint is set of hash value,
     * and body don't contain a value for hashKey null exception validation will be thrown
     * To prevent this from happening, don't validate hash & range key as it is validated by the service itself
     *
     * @param json node to item
     * @param hash value
     * @return to Item
     */
    protected Item toItem(JsonNode json, Object hash) {
        ((ObjectNode) json).putPOJO(hashName, hash);

        // Convert to Object class to validation against Class Type
        T object = JsonUtils.toObject(json, clazz);
        validate(object);

        // Convert validated object back to json to persist
        json = JsonUtils.toTree(object);

        return Item.fromJSON(JsonUtils.toString(json));
    }

    /**
     * @param data to validate with build in validation tool
     * @throws ValidationException validation error
     */
    protected void validate(T data) throws ValidationException {
        ValidationException.validate(data);
    }

    /**
     * @param item to parse
     * @return Parse to T
     */
    @Nullable
    protected T toData(Item item) {
        if (item == null) return null;

        return JsonUtils.toObject(item.toJSON(), clazz);
    }

    /**
     * @param size actual size
     * @return resolved size, cannot be < 1, and not more then max
     */
    protected int resolveSize(int size) {
        if (size < 1 || size > maxSize)
            throw new ParamException("Size cannot be less then 0 or greater than " + maxSize);
        return size;
    }

    protected JsonResult put(Object object) {
        ValidationException.validate(object);
        String json = JsonUtils.toString(object);
        return put(Item.fromJSON(json));
    }

    protected JsonResult put(Item item) {
        table.putItem(item);
        return JsonResult.ok();
    }

    /**
     * @param body       json request body
     * @param primaryKey primary key, hash or hash + range
     * @param fieldNames field names to update
     * @return Updated Date or Null if don't exist
     */
    protected T patch(JsonNode body, PrimaryKey primaryKey, String... fieldNames) {
        return patch(body, primaryKey, s -> {
        }, fieldNames);
    }

    /**
     * @param body         json request body
     * @param primaryKey   primary key, hash or hash + range
     * @param specConsumer consume update item spec to change
     * @param fieldNames   field names to update
     * @return Updated Date or Null if don't exist
     */
    protected T patch(JsonNode body, PrimaryKey primaryKey, Consumer<UpdateItemSpec> specConsumer, String... fieldNames) {
        // Validate Item exists first
        if (table.getItem(primaryKey) == null) return null;

        List<AttributeUpdate> updates = new ArrayList<>();
        for (String fieldName : fieldNames) {
            if (body.has(fieldName)) {
                Object pojo = JsonUtils.toObject(body.path(fieldName), Object.class);
                Object value = valueConformer.transform(pojo);
                updates.add(new AttributeUpdate(fieldName).put(value));
            }
        }

        // If no fields, return null
        if (updates.isEmpty())
            throw new ParamException("No applicable fields to update. " + Arrays.toString(fieldNames));

        // Create Update item spec
        UpdateItemSpec spec = new UpdateItemSpec();
        spec.withPrimaryKey(primaryKey);
        spec.withReturnValues(ReturnValue.ALL_NEW);
        spec.withAttributeUpdate(updates);

        // Spec consumer to edit
        specConsumer.accept(spec);

        try {
            UpdateItemOutcome outcome = table.updateItem(spec);
            return toData(outcome.getItem());
        } catch (ConditionalCheckFailedException e) {
            return null;
        }
    }
}
