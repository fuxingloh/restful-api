package munch.restful.server;

/**
 * Created by: Fuxing
 * Date: 2019-02-12
 * Time: 21:08
 * Project: restful-api
 */
@FunctionalInterface
public interface RequestRoute<T> {

    /**
     * Invoked when a request is made on this route's corresponding path e.g. '/hello'
     *
     * @param call Call object contains request and response object
     * @return The content to be set in the response
     * @throws java.lang.Exception implementation can choose to throw exception
     */
    Object handle(JsonCall call, T request) throws Exception;
}
