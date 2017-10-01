package munch.restful.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import munch.restful.core.JsonUtils;
import munch.restful.core.RestfulMeta;
import munch.restful.core.exception.JsonException;
import spark.ResponseTransformer;
import spark.RouteGroup;
import spark.Spark;

/**
 * Created By: Fuxing Loh
 * Date: 8/2/2017
 * Time: 2:29 PM
 * Project: munch-core
 *
 * @see munch.restful.core.RestfulMeta for structure for meta
 */
public interface JsonService extends RestfulService {
    String APP_JSON = "application/json";

    ObjectMapper objectMapper = JsonUtils.objectMapper;

    /**
     * Theses nodes are pre-build notes, use the as a whole don't
     * put them in another node
     */
    JsonNode Meta200 = JsonTransformer.Meta200;
    JsonNode Meta404 = JsonTransformer.Meta404;

    ResponseTransformer toJson = new JsonTransformer();

    /**
     * Override for custom transformer
     *
     * @return default toJson transformer for json service to use
     */
    default ResponseTransformer toJson() {
        return toJson;
    }

    /**
     * @param path       path to add prefix to route
     * @param routeGroup route
     */
    default void PATH(String path, RouteGroup routeGroup) {
        Spark.path(path, routeGroup);
    }

    /**
     * Map route for HTTP Get
     *
     * @param path  the path
     * @param route json route
     */
    default void GET(String path, JsonRoute route) {
        Spark.get(path, route, toJson());
    }

    /**
     * Map route for HTTP Post
     *
     * @param path  the path
     * @param route json route
     */
    default void POST(String path, JsonRoute route) {
        Spark.post(path, route, toJson());
    }

    /**
     * Map route for HTTP Post
     *
     * @param path  the path
     * @param route json node route
     */
    default void POST(String path, JsonRoute.Node route) {
        Spark.post(path, route, toJson());
    }

    /**
     * Map route for HTTP Put
     *
     * @param path       the path
     * @param acceptType the request accept type
     * @param route      json node route
     */
    default void POST(String path, String acceptType, JsonRoute route) {
        Spark.post(path, acceptType, route, toJson);
    }

    /**
     * Map route for HTTP Put
     *
     * @param path       the path
     * @param acceptType the request accept type
     * @param route      json node route
     */
    default void POST(String path, String acceptType, JsonRoute.Node route) {
        Spark.post(path, acceptType, route, toJson);
    }

    /**
     * Map route for HTTP Put
     *
     * @param path  the path
     * @param route json route
     */
    default void PUT(String path, JsonRoute route) {
        Spark.put(path, route, toJson());
    }

    /**
     * Map route for HTTP Put
     *
     * @param path  the path
     * @param route json node route
     */
    default void PUT(String path, JsonRoute.Node route) {
        Spark.put(path, route, toJson());
    }

    /**
     * Map route for HTTP Put
     *
     * @param path       the path
     * @param acceptType the request accept type
     * @param route      json node route
     */
    default void PUT(String path, String acceptType, JsonRoute route) {
        Spark.put(path, acceptType, route, toJson);
    }

    /**
     * Map route for HTTP Put
     *
     * @param path       the path
     * @param acceptType the request accept type
     * @param route      json node route
     */
    default void PUT(String path, String acceptType, JsonRoute.Node route) {
        Spark.put(path, acceptType, route, toJson);
    }

    /**
     * Map route for HTTP Delete
     *
     * @param path  the path
     * @param route json route
     */
    default void DELETE(String path, JsonRoute route) {
        Spark.delete(path, route, toJson());
    }

    /**
     * Map route for HTTP Delete
     *
     * @param path  the path
     * @param route json node route
     */
    default void DELETE(String path, JsonRoute.Node route) {
        Spark.delete(path, route, toJson());
    }

    /**
     * Map route for HTTP Head
     *
     * @param path  the path
     * @param route json route
     */
    default void HEAD(String path, JsonRoute route) {
        Spark.head(path, route, toJson());
    }

    /**
     * Map route for HTTP Patch
     *
     * @param path  the path
     * @param route json route
     */
    default void PATCH(String path, JsonRoute route) {
        Spark.patch(path, route, toJson());
    }

    /**
     * Read json node to POJO Bean
     *
     * @param node  json node
     * @param clazz Class Type to return
     * @param <T>   Return Class Type
     * @return json body as @code{<T>}
     * @throws JsonException json exception
     */
    default <T> T toObject(JsonNode node, Class<T> clazz) throws JsonException {
        try {
            return objectMapper.treeToValue(node, clazz);
        } catch (JsonProcessingException e) {
            throw new JsonException(e);
        }
    }

    /**
     * @param object object
     * @return json object write to json string
     */
    default JsonNode toTree(Object object) {
        return objectMapper.valueToTree(object);
    }

    /**
     * @return new object node for consumption
     */
    default ObjectNode newNode() {
        return objectMapper.createObjectNode();
    }

    /**
     * @return new array node for consumption
     */
    default ArrayNode newArrayNode() {
        return objectMapper.createArrayNode();
    }

    /**
     * @param meta meta node
     * @return object node with meta only
     */
    default ObjectNode nodes(RestfulMeta meta) {
        ObjectNode nodes = newNode();
        nodes.set("meta", toTree(meta));
        return nodes;
    }

    /**
     * @param meta meta node
     * @param data data node
     * @return object node with meta and data node
     */
    default ObjectNode nodes(RestfulMeta meta, JsonNode data) {
        ObjectNode nodes = newNode();
        nodes.set("meta", toTree(meta));
        nodes.set("data", data);
        return nodes;
    }

    /**
     * @param code code for meta node
     * @param data data node
     * @return object node with meta and data node
     */
    default ObjectNode nodes(int code, JsonNode data) {
        ObjectNode nodes = newNode();
        nodes.set("meta", newNode().put("code", code));
        nodes.set("data", data);
        return nodes;
    }

    /**
     * @param code code
     * @return ObjectNode with meta
     */
    default ObjectNode nodes(int code) {
        ObjectNode nodes = newNode();
        nodes.set("meta", newNode().put("code", code));
        return nodes;
    }

    /**
     * @param code   code for meta node
     * @param object object
     * @return object node with meta and data node
     */
    default ObjectNode nodes(int code, Object object) {
        return nodes(code, toTree(object));
    }
}
