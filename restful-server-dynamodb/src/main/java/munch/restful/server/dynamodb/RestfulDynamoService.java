package munch.restful.server.dynamodb;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.fasterxml.jackson.databind.JsonNode;
import munch.restful.core.JsonUtils;
import munch.restful.core.exception.ValidationException;
import munch.restful.server.JsonService;

import javax.annotation.Nullable;

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

    /**
     * @param table    dynamodb table
     * @param clazz    of data
     * @param hashName range of hash
     */
    protected RestfulDynamoService(Table table, Class<T> clazz, String hashName) {
        this.table = table;
        this.clazz = clazz;
        this.hashName = hashName;
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
        validate(JsonUtils.toObject(json, clazz));

        Item item = Item.fromJSON(JsonUtils.toString(json));
        item.with(hashName, hash);
        return item;
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
}
