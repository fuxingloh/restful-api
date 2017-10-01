package munch.restful.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpMethod;
import com.mashape.unirest.request.HttpRequestWithBody;
import com.mashape.unirest.request.body.MultipartBody;
import munch.restful.core.exception.JsonException;
import munch.restful.core.exception.StructuredException;
import munch.restful.core.exception.UnknownException;
import org.apache.http.entity.ContentType;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created By: Fuxing Loh
 * Date: 18/3/2017
 * Time: 4:13 PM
 * Project: munch-core
 */
public class RestfulRequest {
    protected static final ObjectMapper objectMapper = RestfulClient.objectMapper;

    protected final HttpRequestWithBody request;
    protected MultipartBody multipartBody;

    protected Function<Supplier<RestfulResponse>, RestfulResponse> executor;

    /**
     * @param method http method
     * @param url    base url without /
     */
    public RestfulRequest(HttpMethod method, String url) {
        this.request = new HttpRequestWithBody(method, url);
    }

    /**
     * Set executor to intercept any behaviour
     *
     * @param executor executor for asResponse
     */
    public void setExecutor(Function<Supplier<RestfulResponse>, RestfulResponse> executor) {
        this.executor = executor;
    }

    public RestfulRequest basicAuth(String username, String password) {
        request.basicAuth(username, password);
        return this;
    }

    /**
     * @param name  name in path put {id}
     * @param value value for replace in place
     * @return this
     */
    public RestfulRequest path(String name, String value) {
        request.routeParam(name, value);
        return this;
    }

    /**
     * @param name  name in path put {id}
     * @param value value for replace in place
     * @return this
     */
    public RestfulRequest path(String name, int value) {
        request.routeParam(name, Integer.toString(value));
        return this;
    }

    /**
     * @param name  name in path put {id}
     * @param value value for replace in place
     * @return this
     */
    public RestfulRequest path(String name, long value) {
        request.routeParam(name, Long.toString(value));
        return this;
    }

    /**
     * @param name  name in path put {id}
     * @param value value for replace in place
     * @return this
     */
    public RestfulRequest path(String name, Object value) {
        request.routeParam(name, value.toString());
        return this;
    }

    public RestfulRequest header(String name, String value) {
        request.header(name, value);
        return this;
    }

    public RestfulRequest headers(Map<String, String> headers) {
        request.headers(headers);
        return this;
    }

    public RestfulRequest queryString(Map<String, Object> parameters) {
        request.queryString(parameters);
        return this;
    }

    public RestfulRequest queryString(String name, Object value) {
        request.queryString(name, value);
        return this;
    }

    public RestfulRequest field(String name, Collection<?> value) {
        if (multipartBody == null) multipartBody = request.field(name, value);
        else multipartBody.field(name, value);
        return this;
    }

    public RestfulRequest field(String name, Object value) {
        if (multipartBody == null) multipartBody = request.field(name, value);
        else multipartBody.field(name, value);
        return this;
    }

    public RestfulRequest field(String name, File file) {
        if (multipartBody == null) multipartBody = request.field(name, file);
        else multipartBody.field(name, file);
        return this;
    }

    public RestfulRequest field(String name, File file, String contentType) {
        if (multipartBody == null) multipartBody = request.field(name, file, contentType);
        else multipartBody.field(name, file, contentType);
        return this;
    }

    public RestfulRequest fields(Map<String, Object> parameters) {
        if (multipartBody == null) multipartBody = request.fields(parameters);
        return this;
    }

    public RestfulRequest field(String name, InputStream stream, ContentType contentType, String fileName) {
        if (multipartBody == null) multipartBody = request.field(name, stream, contentType, fileName);
        else multipartBody.field(name, stream, contentType, fileName);
        return this;
    }

    public RestfulRequest field(String name, InputStream stream, String fileName) {
        if (multipartBody == null) multipartBody = request.field(name, stream, fileName);
        else multipartBody.field(name, stream, fileName);
        return this;
    }

    /**
     * @param body raw bytes body
     * @return this
     */
    public RestfulRequest body(byte[] body) {
        request.body(body);
        return this;
    }

    /**
     * @param object object to convert to json
     * @return this
     */
    public RestfulRequest body(Object object) {
        try {
            request.body(objectMapper.writeValueAsBytes(object));
            return this;
        } catch (JsonProcessingException e) {
            throw new JsonException(e);
        }
    }

    public JsonNode asNode() {
        return asResponse().getNode();
    }

    public JsonNode asDataNode() {
        return asResponse().getDataNode();
    }

    public <T> T asDataObject(Class<T> clazz) {
        return asResponse().asDataObject(clazz);
    }

    public <T> List<T> asDataList(Class<T> clazz) {
        return asResponse().asDataList(clazz);
    }

    /**
     * Call service and convert request to response
     * At the same time OfflineException and TimeoutException is parsed
     * Other exception will be parsed to unknown exception
     *
     * @return RestfulResponse
     */
    public RestfulResponse asResponse() {
        return asResponse((restfulResponse, e) -> {
        });
    }

    /**
     * Call service and convert request to response
     * At the same time OfflineException and TimeoutException is parsed
     * Other exception will be parsed to unknown exception
     *
     * @return RestfulResponse
     */
    public RestfulResponse asResponse(BiConsumer<RestfulResponse, StructuredException> handler) {
        if (executor == null) return executeResponse(handler);
        return executor.apply(() -> executeResponse(handler));
    }

    /**
     * Handler will handle StructuredException
     * For non structured exception it will handled by catch(Exception) below
     *
     * @param handler handler of response
     * @return RestfulResponse
     */
    private RestfulResponse executeResponse(BiConsumer<RestfulResponse, StructuredException> handler) {
        try {
            return new RestfulResponse(this, request.asString(), handler);
        } catch (Exception e) {
            // If is structured error just throw
            if (e instanceof StructuredException) {
                throw (StructuredException) e;
            }

            // Try parse error
            ExceptionParser.parse(e);
            throw new UnknownException(e);
        }
    }

    /**
     * Validate meta code of response
     *
     * @param codes codes to validate
     */
    public RestfulResponse hasCode(int... codes) {
        return asResponse().hasCode(codes);
    }
}
