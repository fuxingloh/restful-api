package munch.restful.server;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by: Fuxing
 * Date: 4/6/18
 * Time: 1:31 PM
 * Project: restful-api
 */
public class JsonResult {
    private final int code;
    private Map<String, Object> map;

    protected JsonResult(int code, Map<String, Object> map) {
        this.code = code;
        this.map = map;
    }

    protected int getCode() {
        return code;
    }

    protected Map<String, Object> getMap() {
        return map;
    }

    /**
     * @param name   of field
     * @param object to put
     * @return JsonResult for chaining
     */
    public JsonResult put(String name, Object object) {
        map.put(name, object);
        return this;
    }

    /**
     * @param data json data to return
     * @return JsonResult with 200 status
     */
    public static JsonResult ok(Object data) {
        return of(200).put("data", data);
    }

    /**
     * @param code status code
     * @param data json data
     * @return JsonResult with custom status code & data
     */
    public static JsonResult of(int code, Object data) {
        return of(code).put("data", data);
    }

    /**
     * @param code status code
     * @return JsonResult with custom status code
     */
    public static JsonResult of(int code) {
        return new JsonResult(code, new HashMap<>());
    }

    /**
     * @return JsonResult with 404 status
     */
    public static JsonResult notFound() {
        return of(404);
    }

    /**
     * @return JsonResult with 200 status
     */
    public static JsonResult ok() {
        return of(200);
    }

    @Override
    public String toString() {
        return "JsonResult{" +
                "code=" + code +
                ", map=" + map +
                '}';
    }
}
