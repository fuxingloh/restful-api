package munch.restful.server;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import munch.restful.core.JsonUtils;
import munch.restful.core.RestfulMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.ResponseTransformer;

import java.util.Map;

/**
 * Created by: Fuxing
 * Date: 16/6/2017
 * Time: 1:46 PM
 * Project: munch-core
 */
public class JsonTransformer implements ResponseTransformer {
    private static final Logger logger = LoggerFactory.getLogger(JsonTransformer.class);
    private static final ObjectMapper objectMapper = JsonService.objectMapper;

    static final JsonNode Meta200 =
            objectMapper.createObjectNode()
                    .set("meta", objectMapper.valueToTree(RestfulMeta.builder()
                            .code(200)
                            .build()));
    static final JsonNode Meta404 =
            objectMapper.createObjectNode()
                    .set("meta", objectMapper.valueToTree(RestfulMeta.builder()
                            .code(404).build()));

    public static final String Meta200String = JsonUtils.toString(Meta200);
    public static final String Meta404String = JsonUtils.toString(Meta404);

    /**
     * @param result to convert to string
     * @return converted to string
     */
    public String render(JsonResult result) {
        if (result.getMap() == null || result.getMap().isEmpty()) {
            if (result.getCode() == 200) return Meta200String;
            if (result.getCode() == 404) return Meta404String;
            return JsonUtils.toString(
                    Map.of("meta", Map.of("code", result.getCode()))
            );
        }

        Map<String, Object> map = result.getMap();
        map.put("meta", Map.of("code", result.getCode()));
        return toString(map);
    }

    /**
     * Override this method for custom serialization.
     * - e.g. for simplifying objects
     *
     * @param map to convert to string
     * @return JSON represented in String
     */
    protected String toString(Map<String, Object> map) {
        return JsonUtils.toString(map);
    }

    /**
     * @see JsonResult
     * @see JsonRoute
     */
    @Override
    public String render(Object model) throws Exception {
        // Force casted to JsonResult because technically it should only return JsonResult
        // See JsonRoute
        return render((JsonResult) model);
    }
}
