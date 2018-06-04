package munch.restful.server;

import spark.Filter;
import spark.Request;
import spark.Response;

/**
 * Lambda Route interface
 * <p>
 * Created by: Fuxing
 * Date: 7/3/2017
 * Time: 4:22 PM
 * Project: munch-core
 */
@FunctionalInterface
public interface JsonFilter extends Filter {

    /**
     * Invoked when a request is made on this route's corresponding path e.g. '/hello'
     *
     * @param call Call object contains request and response object
     * @throws Exception implementation can choose to throw exception
     */
    void handle(JsonCall call) throws Exception;

    @Override
    default void handle(Request request, Response response) throws Exception {
        handle(new JsonCall(request, response));
    }
}