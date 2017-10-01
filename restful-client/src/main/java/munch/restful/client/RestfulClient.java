package munch.restful.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpMethod;
import munch.restful.core.JsonUtils;

/**
 * Created By: Fuxing Loh
 * Date: 18/3/2017
 * Time: 3:42 PM
 * Project: munch-core
 */
public abstract class RestfulClient {
    protected static final ObjectMapper objectMapper = JsonUtils.objectMapper;

    protected final String url;

    /**
     * @param url must not end with /
     */
    public RestfulClient(String url) {
        this.url = url;
    }

    /**
     * @param path path must begin with /
     * @return full path
     */
    private String path(String path) {
        return url + path;
    }

    /**
     * Parse and do something to the request
     *
     * @param request request
     * @return same or another request
     */
    protected RestfulRequest before(RestfulRequest request) {
        return request;
    }

    protected RestfulRequest doGet(String path) {
        RestfulRequest request = new RestfulRequest(HttpMethod.GET, path(path));
        return before(request);
    }

    protected RestfulRequest doHead(String path) {
        RestfulRequest request = new RestfulRequest(HttpMethod.HEAD, path(path));
        return before(request);
    }

    protected RestfulRequest doOptions(String path) {
        RestfulRequest request = new RestfulRequest(HttpMethod.OPTIONS, path(path));
        return before(request);
    }

    protected RestfulRequest doPost(String path) {
        RestfulRequest request = new RestfulRequest(HttpMethod.POST, path(path));
        return before(request);
    }

    protected RestfulRequest doDelete(String path) {
        RestfulRequest request = new RestfulRequest(HttpMethod.DELETE, path(path));
        return before(request);
    }

    protected RestfulRequest doPatch(String path) {
        RestfulRequest request = new RestfulRequest(HttpMethod.PATCH, path(path));
        return before(request);
    }

    protected RestfulRequest doPut(String path) {
        RestfulRequest request = new RestfulRequest(HttpMethod.PUT, path(path));
        return before(request);
    }
}
