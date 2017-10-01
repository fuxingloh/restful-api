package munch.restful.server;

import com.fasterxml.jackson.databind.JsonNode;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * Lambda Route interface
 * <p>
 * Created by: Fuxing
 * Date: 7/3/2017
 * Time: 4:22 PM
 * Project: munch-core
 */
@FunctionalInterface
public interface JsonRoute extends Route {

    /**
     * Invoked when a request is made on this route's corresponding path e.g. '/hello'
     *
     * @param call Call object contains request and response object
     * @return The content to be set in the response
     * @throws java.lang.Exception implementation can choose to throw exception
     */
    Object handle(JsonCall call) throws Exception;

    @Override
    default Object handle(Request request, Response response) throws Exception {
        return handle(new JsonCall(request, response));
    }

    @FunctionalInterface
    interface Node extends JsonRoute {

        /**
         * Invoked when a request is made on this route's corresponding path e.g. '/hello'
         *
         * @param call call object contains request and response object
         * @param node the json node itself
         * @return The json content to be set in the response
         * @throws java.lang.Exception implementation can choose to throw exception
         */
        Object handle(JsonCall call, JsonNode node) throws Exception;

        default Object handle(JsonCall call) throws Exception {
            return handle(call, call.bodyAsJson());
        }
    }
}