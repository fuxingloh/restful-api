package munch.restful.server;

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
    String APP_JSON = "application/json";

    /**
     * Invoked when a request is made on this route's corresponding path e.g. '/hello'
     *
     * @param call Call object contains request and response object
     * @return The content to be set in the response
     * @throws java.lang.Exception implementation can choose to throw exception
     */
    Object handle(JsonCall call) throws Exception;

    @Override
    default JsonResult handle(Request request, Response response) throws Exception {
        Object result = handle(new JsonCall(request, response));
        response.type(APP_JSON);

        if (result instanceof JsonResult) {
            response.status(((JsonResult) result).getCode());
            return (JsonResult) result;
        }

        if (result == null) return JsonResult.notFound();
        return JsonResult.ok(result);
    }
}