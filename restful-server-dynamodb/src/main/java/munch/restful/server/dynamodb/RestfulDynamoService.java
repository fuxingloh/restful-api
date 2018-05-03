package munch.restful.server.dynamodb;

import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.api.QueryApi;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import munch.restful.core.JsonUtils;
import munch.restful.core.exception.ParamException;
import munch.restful.server.JsonCall;
import munch.restful.server.JsonService;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    protected final String rangeName;

    protected final int maxSize;

    /**
     * @param table     dynamodb table
     * @param clazz     of data
     * @param hashName  name of hash
     * @param rangeName name of range
     */
    protected RestfulDynamoService(Table table, Class<T> clazz, String hashName, String rangeName) {
        this(table, clazz, hashName, rangeName, 100);
    }

    /**
     * @param table     dynamodb table
     * @param clazz     of data
     * @param hashName  range of hash
     * @param rangeName range of range
     * @param maxSize   max size per query
     */
    protected RestfulDynamoService(Table table, Class<T> clazz, String hashName, String rangeName, int maxSize) {
        this.table = table;
        this.clazz = clazz;
        this.hashName = hashName;
        this.rangeName = rangeName;
        this.maxSize = maxSize;
    }

    /**
     * @param call json call with pathString(hashName), queryString(rangeName, null), and queryInt(size, 20)
     * @return JsonNode result to return
     * @see RestfulDynamoService#list(JsonCall)
     */
    protected JsonNode list(JsonCall call) {
        return list(table,
                call.pathString(hashName),
                call.queryString(rangeName, null),
                call.queryInt("size", 20));
    }

    /**
     * @param queryApi for querying, can be a table or index
     * @param hash     hash value
     * @param range    exclusive range start value, nullable means start from top
     * @param size     size per list
     * @return JsonNode result to return
     * @see QueryApi#query(QuerySpec)
     * @see Table this extends QueryApi
     * @see Index this extends QueryApi
     */
    protected JsonNode list(QueryApi queryApi, Object hash, @Nullable Object range, int size) {
        QuerySpec querySpec = new QuerySpec();
        querySpec.withScanIndexForward(false);
        querySpec.withHashKey(hashName, hash);
        querySpec.withMaxPageSize(resolveSize(size));

        if (range != null) {
            querySpec.withExclusiveStartKey(hashName, hash, rangeName, range);
        }

        List<Item> items = new ArrayList<>();
        queryApi.query(querySpec).forEach(items::add);


        List<T> dataList = items.stream().map(this::parse).collect(Collectors.toList());
        ObjectNode node = nodes(200, dataList);

        // If no more next
        if (items.size() != size) return node;

        // Have next, send next object
        ObjectNode next = node.putObject("next");
        next.putPOJO(hashName, hash);
        next.putPOJO(rangeName, items.get(size - 1).get(rangeName));
        return node;
    }

    /**
     * @param call json call with pathString(hashName) and pathString(rangeName)
     * @return Object or nullable
     * @see RestfulDynamoService#get(Object, Object)
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
        Item item = table.getItem(hashName, hash, rangeName, range);
        return parse(item);
    }

    /**
     * @param call json call with pathString(hashName), pathString(rangeName), and request body
     * @return Object saved
     * @see RestfulDynamoService#put(Object, Object, String)
     */
    protected T put(JsonCall call) {
        return put(call.pathString(hashName), call.pathString(rangeName), call.request().body());
    }

    /**
     * @param hash  value
     * @param range value
     * @param json  body
     * @return Object saved
     */
    protected T put(Object hash, Object range, String json) {
        Item item = parse(hash, range, json);
        PutItemOutcome outcome = table.putItem(item);
        return parse(outcome.getItem());
    }

    /**
     * @param call json call with pathString(hashName) and pathString(rangeName)
     * @return Deleted Data or null if not found
     * @see RestfulDynamoService#delete(Object, Object)
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
        DeleteItemOutcome outcome = table.deleteItem(hashName, hash, rangeName, range);
        return parse(outcome.getItem());
    }

    /**
     * @param item to parse
     * @return Parse to T
     */
    @Nullable
    protected T parse(Item item) {
        if (item == null) return null;

        return JsonUtils.toObject(item.toJSON(), clazz);
    }

    /**
     * @param hash  primary hash key
     * @param range secondary range key
     * @param json  body
     * @return Parsed Item
     */
    protected Item parse(Object hash, Object range, String json) {
        Item item = Item.fromJSON(json);
        item.with(hashName, hash);
        item.with(rangeName, range);
        return item;
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
}
