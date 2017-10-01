package munch.restful.client;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.Headers;
import com.mashape.unirest.http.HttpResponse;
import munch.restful.core.JsonUtils;
import munch.restful.core.RestfulMeta;
import munch.restful.core.exception.JsonException;
import munch.restful.core.exception.StructuredException;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created By: Fuxing Loh
 * Date: 18/3/2017
 * Time: 4:19 PM
 * Project: munch-core
 */
public class RestfulResponse {
    protected static final ObjectMapper objectMapper = RestfulClient.objectMapper;

    private final JsonNode jsonNode;
    private final RestfulMeta meta;
    private final HttpResponse<String> response;

    /**
     * For error parser, as long there is error node it is consider a error and will be converted to Structured Error
     * <pre>
     * {
     *      meta: {
     *          code: 200 or 404
     *          error: {} // THIS
     *      }
     * }
     * </pre>
     *
     * @param response unirest response
     * @param handler  handler for error
     */
    RestfulResponse(RestfulRequest request, HttpResponse<String> response, BiConsumer<RestfulResponse, StructuredException> handler) {
        this.response = response;
        try {
            this.jsonNode = objectMapper.readTree(response.getBody());
            try {
                meta = JsonUtils.toObject(getNode().path("meta"), RestfulMeta.class);
            } catch (JsonException e) {
                throw new RuntimeException("RestfulMeta cannot be parsed.", e);
            }
        } catch (IOException e) {
            // Can be added to handle 504 error from AWS ELB
            // if (response.getStatus() == 504) throw new TimeoutException(e);
            throw new JsonException(e, request.request.getUrl());
        }

        // Set structured error
        StructuredException structured = meta.getError() == null ? null
                : StructuredException.fromMeta(meta, request.request.getUrl());

        // Run through handler
        handler.accept(this, structured);

        // Then else if structured exist, throw it
        if (structured != null) throw structured;
    }

    public int getStatus() {
        return response.getStatus();
    }

    /**
     * @return Response Headers (map) with <b>same case</b> as server response.
     * For instance use <code>getHeaders().getFirst("Location")</code> and not <code>getHeaders().getFirst("location")</code> to get first header "Location"
     */
    public Headers getHeaders() {
        return response.getHeaders();
    }

    /**
     * @return get first header
     */
    public String getHeader(Object key) {
        return getHeaders().getFirst(key);
    }

    /**
     * @return json node
     */
    public JsonNode getNode() {
        return jsonNode;
    }

    public RestfulMeta getMeta() {
        return meta;
    }

    /**
     * @return json node
     */
    public JsonNode getDataNode() {
        return getNode().path("data");
    }

    /**
     * @param clazz class of object
     * @param <T>   type of object
     * @return object if data node is present
     * return null if node is null or missing
     */
    public <T> T asDataObject(Class<T> clazz) {
        try {
            JsonNode data = getDataNode();
            if (data.isNull() || data.isMissingNode()) return null;
            return objectMapper.treeToValue(data, clazz);
        } catch (JsonProcessingException e) {
            throw new JsonException(e);
        }
    }

    /**
     * @param clazz class of array type
     * @param <T>   Type
     * @return List of given type
     */
    public <T> List<T> asDataList(Class<T> clazz) {
        return JsonUtils.toList(getDataNode(), clazz);
    }

    /**
     * @param mapper map from root node to any result
     * @param <T>    Type to return
     * @return Type
     */
    public <T> T as(Function<JsonNode, T> mapper) {
        return mapper.apply(getNode());
    }

    /**
     * Validate meta code of response
     *
     * @param codes codes to validate
     */
    public RestfulResponse hasCode(int... codes) {
        int code = getMeta().getCode();
        for (int i : codes) {
            if (i == code) return this;
        }

        String codeList = Arrays.stream(codes)
                .mapToObj(Integer::toString)
                .collect(Collectors.joining(", "));
        throw new StructuredException(code, "CodeException", "Explicit validation on code(" + codeList + ") failed.");
    }
}