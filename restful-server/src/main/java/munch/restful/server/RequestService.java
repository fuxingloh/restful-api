package munch.restful.server;

/**
 * Created by: Fuxing
 * Date: 2019-02-12
 * Time: 21:02
 * Project: restful-api
 */
public interface RequestService<R> extends JsonService {

    R request(JsonCall call);

    private JsonRoute jsonRoute(RequestRoute<R> route) {
        return call -> route.handle(call, request(call));
    }

    /**
     * Map route for HTTP Get
     *
     * @param path  the path
     * @param route json route
     */
    default void GET(String path, RequestRoute<R> route) {
        GET(path, jsonRoute(route));
    }

    /**
     * Map route for HTTP Post
     *
     * @param path  the path
     * @param route json route
     */
    default void POST(String path, RequestRoute<R> route) {
        POST(path, jsonRoute(route));
    }

    /**
     * Map route for HTTP Put
     *
     * @param path       the path
     * @param acceptType the request accept type
     * @param route      json node route
     */
    default void POST(String path, String acceptType, RequestRoute<R> route) {
        POST(path, acceptType, jsonRoute(route));
    }

    /**
     * Map route for HTTP Put
     *
     * @param path  the path
     * @param route json route
     */
    default void PUT(String path, RequestRoute<R> route) {
        PUT(path, jsonRoute(route));
    }

    /**
     * Map route for HTTP Put
     *
     * @param path       the path
     * @param acceptType the request accept type
     * @param route      json node route
     */
    default void PUT(String path, String acceptType, RequestRoute<R> route) {
        PUT(path, acceptType, jsonRoute(route));
    }

    /**
     * Map route for HTTP Delete
     *
     * @param path  the path
     * @param route json route
     */
    default void DELETE(String path, RequestRoute<R> route) {
        DELETE(path, jsonRoute(route));
    }

    /**
     * Map route for HTTP Head
     *
     * @param path  the path
     * @param route json route
     */
    default void HEAD(String path, RequestRoute<R> route) {
        HEAD(path, jsonRoute(route));
    }

    /**
     * Map route for HTTP Patch
     *
     * @param path  the path
     * @param route json route
     */
    default void PATCH(String path, RequestRoute<R> route) {
        PATCH(path, jsonRoute(route));
    }
}
