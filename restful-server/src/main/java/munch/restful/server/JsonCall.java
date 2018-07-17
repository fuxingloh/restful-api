package munch.restful.server;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import munch.restful.core.JsonUtils;
import munch.restful.core.exception.CodeException;
import munch.restful.core.exception.JsonException;
import munch.restful.core.exception.ParamException;
import org.apache.commons.lang3.StringUtils;
import spark.Request;
import spark.Response;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;

/**
 * Created by: Fuxing
 * Date: 17/3/2017
 * Time: 1:23 AM
 * Project: munch-core
 */
public class JsonCall {
    private static final ObjectMapper objectMapper = JsonService.objectMapper;

    private final Request request;
    private final Response response;

    /**
     * @param request  spark request
     * @param response spark response
     */
    JsonCall(Request request, Response response) {
        this.request = request;
        this.response = response;
    }

    /**
     * @return Spark request
     */
    public Request request() {
        return request;
    }

    /**
     * @return Spark response
     */
    public Response response() {
        return response;
    }

    /**
     * @return request body as JsonNode
     * @throws JsonException json exception
     */
    public JsonNode bodyAsJson() {
        try {
            return objectMapper.readTree(request.bodyAsBytes());
        } catch (IOException e) {
            throw new JsonException(e);
        }
    }

    /**
     * @return request body as json object
     */
    public <T> T bodyAsObject(Class<T> clazz) {
        try {
            return objectMapper.readValue(request.bodyAsBytes(), clazz);
        } catch (IOException e) {
            throw new JsonException(e);
        }
    }

    /**
     * @param clazz clazz
     * @param <T>   Type
     * @return List as type
     */
    public <T> List<T> bodyAsList(Class<T> clazz) {
        return JsonUtils.toList(bodyAsJson(), clazz);
    }

    /**
     * @param mapper json mapper
     * @param <T>    Type
     * @return List as type
     */
    public <T> List<T> bodyAsList(Function<JsonNode, T> mapper) {
        return JsonUtils.toList(bodyAsJson(), mapper);
    }

    /**
     * @param name name of query string
     * @return long value from query string
     * @throws ParamException query param not found
     */
    public long queryLong(String name) throws ParamException {
        try {
            String value = queryString(name);
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new ParamException(name);
        }
    }

    /**
     * @param name         name of query string
     * @param defaultValue default long value if not found
     * @return long value from query string
     * @throws ParamException query param not found
     */
    public long queryLong(String name, long defaultValue) throws ParamException {
        try {
            String value = request.queryParams(name);
            if (StringUtils.isBlank(value)) return defaultValue;
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new ParamException(name);
        }
    }

    /**
     * @param name name of query string
     * @return integer value from query string
     * @throws ParamException query param not found
     */
    public int queryInt(String name) throws ParamException {
        try {
            String value = queryString(name);
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new ParamException(name);
        }
    }

    /**
     * @param name         name of query string
     * @param defaultValue default int value if not found
     * @return int value from query string
     * @throws ParamException query param not found
     */
    public int queryInt(String name, int defaultValue) throws ParamException {
        try {
            String value = request.queryParams(name);
            if (StringUtils.isBlank(value)) return defaultValue;
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new ParamException(name);
        }
    }

    /**
     *
     * @param defaultSize default size if not present
     * @param maxSize max size if present
     * @return size value from query string
     */
    public int querySize(int defaultSize, int maxSize) {
        int size = queryInt("size", defaultSize);
        if (size <= 0) return defaultSize;
        if (size >= maxSize) return maxSize;
        return size;
    }

    /**
     * @param name name of query string
     * @return double value from query string
     * @throws ParamException query param not found
     */
    public double queryDouble(String name) throws ParamException {
        try {
            String value = queryString(name);
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new ParamException(name);
        }
    }

    /**
     * @param name         name of query string
     * @param defaultValue default double value if not found
     * @return double value from query string
     * @throws ParamException query param not found
     */
    public double queryDouble(String name, double defaultValue) throws ParamException {
        try {
            String value = queryString(name);
            if (StringUtils.isBlank(value)) return defaultValue;
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new ParamException(name);
        }
    }

    /**
     * Boolean query string by checking string.equal("true")
     *
     * @param name name of query string
     * @return boolean value from query string
     * @throws ParamException query param not found
     */
    public boolean queryBool(String name) throws ParamException {
        return Boolean.parseBoolean(queryString(name));
    }

    /**
     * Boolean query string by checking string.equal("true")
     *
     * @param name         name of query string
     * @param defaultValue default boolean value if not found
     * @return boolean value from query string
     * @throws ParamException query param not found
     */
    public boolean queryBool(String name, boolean defaultValue) throws ParamException {
        String value = request.queryParams(name);
        if (StringUtils.isBlank(value)) return defaultValue;
        return Boolean.parseBoolean(value);
    }

    /**
     * @param name name of query string
     * @return String value
     * @throws ParamException query param not found
     */
    public String queryString(String name) throws ParamException {
        String value = request.queryParams(name);
        if (StringUtils.isNotBlank(value)) {
            return value;
        }
        throw new ParamException(name);
    }

    /**
     * @param name         name of query string
     * @param defaultValue default String value
     * @return String value
     */
    public String queryString(String name, String defaultValue) throws ParamException {
        String value = request.queryParams(name);
        if (StringUtils.isNotBlank(value)) {
            return value;
        }
        return defaultValue;
    }

    /**
     * @param name         of query string
     * @param defaultValue to return if not found
     * @param clazz        class to bound Object to
     * @param <T>          T
     * @return Object value
     */
    @SuppressWarnings("unchecked")
    public <T> T queryObject(String name, T defaultValue, Class<T> clazz) {
        String value = request.queryParams(name);
        if (StringUtils.isBlank(value)) return defaultValue;
        if (clazz == String.class) return (T) value;

        try {
            if (clazz == Long.class) {
                Long i = Long.parseLong(value);
                return (T) i;
            }
            if (clazz == Integer.class) {
                Integer i = Integer.parseInt(value);
                return (T) i;
            }
        } catch (NumberFormatException e) {
            throw new ParamException(name);
        }

        if (clazz == Boolean.class) {
            Boolean b = Boolean.parseBoolean(value);
            return (T) b;
        }

        throw new IllegalStateException(clazz.getSimpleName() + " is not implemented for queryObject()");
    }

    /**
     * @param name name of path param
     * @return Long value
     * @throws ParamException path param not found
     */
    public long pathLong(String name) throws ParamException {
        try {
            String value = pathString(name);
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new ParamException(name);
        }
    }

    /**
     * @param name name of path param
     * @return Int value
     * @throws ParamException path param not found
     */
    public int pathInt(String name) throws ParamException {
        try {
            String value = pathString(name);
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new ParamException(name);
        }
    }

    /**
     * @param name name of path param
     * @return Double value
     * @throws ParamException path param not found
     */
    public double pathDouble(String name) throws ParamException {
        try {
            String value = pathString(name);
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new ParamException(name);
        }
    }

    /**
     * @param name name of path param
     * @return String value
     * @throws ParamException path param not found
     */
    public String pathString(String name) throws ParamException {
        String value = request.params(name);
        if (StringUtils.isNotBlank(value)) {
            return value;
        }
        throw new ParamException(name);
    }

    /**
     * @param name name of header
     * @return nullable string header
     */
    public String getHeader(String name) {
        return request().headers(name);
    }

    /**
     * @return DecodedJWT if found else null
     */
    public DecodedJWT getJWT() {
        String token = getJWTToken(this);
        if (token == null) return null;

        try {
            return JWT.decode(token);
        } catch (JWTDecodeException exception) {
            // Invalid token
            throw new CodeException(403);
        }
    }

    /**
     * @param call Authorization get from header
     * @return token or null if don't exist
     */
    @Nullable
    private static String getJWTToken(JsonCall call) {
        final String value = call.getHeader("Authorization");
        if (value == null || !value.toLowerCase().startsWith("bearer")) {
            return null;
        }

        String[] parts = value.split(" ");
        if (parts.length < 2) {
            return null;
        }

        return parts[1].trim();
    }
}
