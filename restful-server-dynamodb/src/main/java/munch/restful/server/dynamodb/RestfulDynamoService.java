package munch.restful.server.dynamodb;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.RangeKeyCondition;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.api.QueryApi;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import munch.restful.core.JsonUtils;
import munch.restful.core.NextNodeList;
import munch.restful.core.exception.ConflictException;
import munch.restful.core.exception.ParamException;
import munch.restful.core.exception.ValidationException;
import munch.restful.server.JsonCall;
import munch.restful.server.JsonService;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by: Fuxing
 * Date: 2/5/18
 * Time: 7:08 PM
 * Project: munch-partners
 */
public abstract class RestfulDynamoService<T> implements JsonService {
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
    protected NextNodeList<T> list(QueryApi queryApi, String hashName, String rangeName, JsonCall call) {
        return list(queryApi,
                hashName, call.pathString(hashName),
                rangeName, call.queryString("next." + rangeName, null),
                querySize(call)
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
    protected NextNodeList<T> list(QueryApi queryApi, String hashName, Object hash, String rangeName, @Nullable Object nextRange, int size) {
        ParamException.requireNonNull(hashName, hash);

        QuerySpec querySpec = new QuerySpec();
        querySpec.withScanIndexForward(false);
        querySpec.withHashKey(hashName, hash);
        querySpec.withMaxResultSize(size);

        if (nextRange != null) {
            querySpec.withRangeKeyCondition(new RangeKeyCondition(rangeName).lt(nextRange));
        }

        List<T> dataList = new ArrayList<>();
        Item lastItem = null;
        for (Item item : queryApi.query(querySpec)) {
            lastItem = item;
            dataList.add(toData(item));
        }

        if (lastItem == null || dataList.size() != size) {
            // If no more next
            return new NextNodeList<>(dataList);
        } else {
            // Have next, send next object
            return new NextNodeList<>(dataList, rangeName, lastItem.get(rangeName));
        }
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
     * @param call to get size
     * @return size
     */
    protected int querySize(JsonCall call) {
        return call.querySize(20, maxSize);
    }

    protected abstract T get(JsonCall call);

    protected abstract T get(Item item);

    /**
     * @param object to put with validation
     * @return the same object
     */
    protected T put(T object) {
        ValidationException.validate(object);
        String json = JsonUtils.toString(object);
        Item item = Item.fromJSON(json);
        table.putItem(item);
        return object;
    }

    /**
     * @param object to put with data and key validation, key validation check that existing keys don't exist.
     * @return the same object.
     */
    protected T post(T object) {
        ValidationException.validate(object);
        String json = JsonUtils.toString(object);
        Item item = Item.fromJSON(json);

        if (get(item) != null) throw new ConflictException("Key already exists");

        table.putItem(item);
        return object;
    }

    /**
     * @param fields available for update
     * @return a Function to perform patching
     */
    protected Function<JsonCall, T> patch(String... fields) {
        return call -> patch(call, fields);
    }

    /**
     * @param call   the json call
     * @param fields available for update
     * @return Updated Object
     */
    protected T patch(JsonCall call, String... fields) {
        return patch(call, t -> {
        }, fields);
    }

    /**
     * @param call     the json call
     * @param consumer to make anymore changes before the update
     * @param fields   available for update
     * @return Updated Object
     */
    protected T patch(JsonCall call, Consumer<T> consumer, String... fields) {
        Objects.requireNonNull(consumer);

        T object = get(call);
        if (object == null) return null;

        object = patch(object, call.bodyAsJson(), fields);
        consumer.accept(object);
        return put(object);
    }

    /**
     * @param object to update
     * @param body   to read update from
     * @param fields available for update
     * @return Updated Object
     */
    protected T patch(T object, JsonNode body, String... fields) {
        ObjectNode current = JsonUtils.toTree(object);
        for (String field : fields) {
            if (field.equals(hashName)) continue;
            if (!body.has(field)) continue;

            current.set(field, body.path(field));
        }

        return JsonUtils.toObject(current, clazz);
    }
}
