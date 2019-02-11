package munch.restful.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import munch.restful.core.JsonUtils;
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
public interface JsonService<R extends JsonRoute> extends RestfulService {
    ObjectMapper objectMapper = JsonUtils.objectMapper;

    JsonTransformer toJson = new JsonTransformer();

    /**
     * Override for custom transformer
     *
     * @return default toJson transformer for json service to use
     */
    default JsonTransformer toJson() {
        return toJson;
    }

    /**
     * @param path   path for before filter, accepts wildcards
     * @param filter json filter
     */
    default void BEFORE(String path, JsonFilter filter) {
        Spark.before(path, filter);
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
    default void GET(String path, R route) {
        Spark.get(path, route, toJson());
    }

    /**
     * Map route for HTTP Post
     *
     * @param path  the path
     * @param route json route
     */
    default void POST(String path, R route) {
        Spark.post(path, route, toJson());
    }

    /**
     * Map route for HTTP Put
     *
     * @param path       the path
     * @param acceptType the request accept type
     * @param route      json node route
     */
    default void POST(String path, String acceptType, R route) {
        Spark.post(path, acceptType, route, toJson);
    }

    /**
     * Map route for HTTP Put
     *
     * @param path  the path
     * @param route json route
     */
    default void PUT(String path, R route) {
        Spark.put(path, route, toJson());
    }

    /**
     * Map route for HTTP Put
     *
     * @param path       the path
     * @param acceptType the request accept type
     * @param route      json node route
     */
    default void PUT(String path, String acceptType, R route) {
        Spark.put(path, acceptType, route, toJson);
    }

    /**
     * Map route for HTTP Delete
     *
     * @param path  the path
     * @param route json route
     */
    default void DELETE(String path, R route) {
        Spark.delete(path, route, toJson());
    }

    /**
     * Map route for HTTP Head
     *
     * @param path  the path
     * @param route json route
     */
    default void HEAD(String path, R route) {
        Spark.head(path, route, toJson());
    }

    /**
     * Map route for HTTP Patch
     *
     * @param path  the path
     * @param route json route
     */
    default void PATCH(String path, R route) {
        Spark.patch(path, route, toJson());
    }

    /**
     * @param code status code
     * @return JsonResult
     */
    default JsonResult result(int code) {
        return JsonResult.of(code);
    }

    /**
     * @param code   status code
     * @param object data object
     * @return JsonResult
     */
    default JsonResult result(int code, Object object) {
        return JsonResult.of(code, object);
    }
}
