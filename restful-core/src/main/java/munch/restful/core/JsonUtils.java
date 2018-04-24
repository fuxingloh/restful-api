package munch.restful.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;
import munch.restful.core.exception.JsonException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Created By: Fuxing Loh
 * Date: 16/6/2017
 * Time: 3:48 PM
 * Project: munch-core
 */
public final class JsonUtils {
    public static final ObjectMapper objectMapper = new ObjectMapper();

    public static String toString(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new JsonException(e);
        }
    }

    public static JsonNode readTree(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (IOException e) {
            throw new JsonException(e);
        }
    }

    public static JsonNode toTree(Object object) {
        return objectMapper.valueToTree(object);
    }

    public static <T> T toObject(JsonNode node, Class<T> clazz) {
        try {
            return objectMapper.treeToValue(node, clazz);
        } catch (IOException e) {
            throw new JsonException(e);
        }
    }

    public static <T> T toObject(String value, Class<T> clazz) {
        try {
            return objectMapper.readValue(value, clazz);
        } catch (IOException e) {
            throw new JsonException(e);
        }
    }

    public static <T> List<T> toList(JsonNode nodes, Class<T> clazz) {
        try {
            CollectionType type = objectMapper.getTypeFactory().constructCollectionType(List.class, clazz);
            return objectMapper.convertValue(nodes, type);
        } catch (IllegalArgumentException e) {
            throw new JsonException(e);
        }
    }

    public static <T> List<T> toList(String json, Class<T> clazz) {
        try {
            CollectionType type = objectMapper.getTypeFactory().constructCollectionType(List.class, clazz);
            return objectMapper.readValue(json, type);
        } catch (IOException e) {
            throw new JsonException(e);
        }
    }

    public static <T> List<T> toList(JsonNode nodes, Function<JsonNode, T> mapper) {
        List<T> list = new ArrayList<>();
        for (JsonNode node : nodes) {
            list.add(mapper.apply(node));
        }
        return list;
    }

    public static <K, V> Map<K, V> toMap(JsonNode nodes, Class<K> keyClass, Class<V> valueClass) {
        try {
            MapType type = objectMapper.getTypeFactory().constructMapType(HashMap.class, keyClass, valueClass);
            return objectMapper.convertValue(nodes, type);
        } catch (IllegalArgumentException e) {
            throw new JsonException(e);
        }
    }

    public static <K, V> Map<K, V> toMap(String json, Class<K> keyClass, Class<V> valueClass) {
        try {
            MapType type = objectMapper.getTypeFactory().constructMapType(HashMap.class, keyClass, valueClass);
            return objectMapper.readValue(json, type);
        } catch (IOException e) {
            throw new JsonException(e);
        }
    }
}
