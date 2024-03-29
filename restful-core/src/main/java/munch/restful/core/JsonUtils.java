package munch.restful.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;
import munch.restful.core.exception.JsonException;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created By: Fuxing Loh
 * Date: 16/6/2017
 * Time: 3:48 PM
 * Project: munch-core
 */
public final class JsonUtils {
    /**
     * Singleton ObjectMapper for all restful-api tools to use
     */
    public static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Exactly the same as
     * <pre>
     *     ObjectNode root = createObjectNode();
     *     root.set(name, node);
     *     return root;
     * </pre>
     *
     * @param name to name to node to be wrapped
     * @param node to wrap
     * @return wrapped node
     */
    public static ObjectNode wrap(String name, JsonNode node) {
        ObjectNode root = createObjectNode();
        root.set(name, node);
        return root;
    }

    /**
     * @param rootConsumer to manipulate the created ObjectNode
     * @return created ObjectNode
     */
    public static ObjectNode createObjectNode(Consumer<ObjectNode> rootConsumer) {
        ObjectNode objectNode = createObjectNode();
        rootConsumer.accept(objectNode);
        return objectNode;
    }

    /**
     * Exactly the same as
     * <pre>
     *     ObjectMapper mapper = new ObjectMapper();
     *     ObjectNode objectNode = mapper.createObjectNode();
     * </pre>
     *
     * @return newly created ObjectNode
     */
    public static ObjectNode createObjectNode() {
        return objectMapper.createObjectNode();
    }

    /**
     * Exactly the same as
     * <pre>
     *     ObjectMapper mapper = new ObjectMapper();
     *     ArrayNode arrayNode = mapper.createArrayNode();
     * </pre>
     *
     * @return newly created ArrayNode
     */
    public static ArrayNode createArrayNode() {
        return objectMapper.createArrayNode();
    }

    /**
     * @param object JsonNode or POJO
     * @return JSON String
     */
    public static String toString(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new JsonException(e);
        }
    }

    /**
     * Read json string into JsonNode
     *
     * @param json JSON String
     * @return JsonNode
     * @deprecated use stringToTree, readTree is too fucking confusing
     */
    @Deprecated
    public static JsonNode readTree(String json) {
        return jsonToTree(json);
    }

    /**
     * Read json string to JsonNode
     *
     * @param json JSON String
     * @return JsonNode
     */
    public static JsonNode jsonToTree(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (IOException e) {
            throw new JsonException(e);
        }
    }

    /**
     * @param object POJO into JsonNode
     * @param <T>    Node Type
     * @return JsonNode
     * @deprecated use valueToTree, readTree is too fucking confusing
     */
    @Deprecated
    public static <T extends JsonNode> T toTree(Object object) {
        return valueToTree(object);
    }

    /**
     * @param object POJO into JsonNode
     * @param <T>    Node Type
     * @return JsonNode
     */
    public static <T extends JsonNode> T valueToTree(Object object) {
        return objectMapper.valueToTree(object);
    }

    public static JsonNode validate(JsonNode node, Class<?> clazz) {
        return toTree(toObject(node, clazz));
    }

    public static <T> T deepCopy(T object, Class<T> clazz) {
        return toObject(toTree(object), clazz);
    }

    public static <T> T toObject(JsonNode node, Class<T> clazz) {
        if (node == null) return null;
        try {
            return objectMapper.treeToValue(node, clazz);
        } catch (IOException e) {
            throw new JsonException(e);
        }
    }

    public static <T> T toObject(String value, Class<T> clazz) {
        if (value == null) return null;
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

    public static <T> Set<T> toSet(JsonNode nodes, Class<T> clazz) {
        try {
            CollectionType type = objectMapper.getTypeFactory().constructCollectionType(Set.class, clazz);
            return objectMapper.convertValue(nodes, type);
        } catch (IllegalArgumentException e) {
            throw new JsonException(e);
        }
    }

    public static <T> Set<T> toSet(String json, Class<T> clazz) {
        try {
            CollectionType type = objectMapper.getTypeFactory().constructCollectionType(Set.class, clazz);
            return objectMapper.readValue(json, type);
        } catch (IOException e) {
            throw new JsonException(e);
        }
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
