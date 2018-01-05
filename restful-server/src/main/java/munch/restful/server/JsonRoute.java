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
        Object object = handle(new JsonCall(request, response));
        if (object == null) return null;

        if (object instanceof JsonNode) {
            // Set status code
            response.status(((JsonNode) object).path("meta")
                    .path("code").asInt(200));
        }
        return object;
    }
}