package munch.restful.server;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import munch.restful.core.JsonUtils;
import munch.restful.core.RestfulMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.ResponseTransformer;

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

    public String render(JsonResult result) {
        if (result.getMap() == null || result.getMap().isEmpty()) {
            if (result.getCode() == 200) return Meta200String;
            if (result.getCode() == 404) return Meta404String;

            ObjectNode body = JsonUtils.createObjectNode();
            body.putObject("meta").put("code", result.getCode());
            return JsonUtils.toString(body);
        }

        ObjectNode body = JsonUtils.createObjectNode();
        body.putObject("meta").put("code", result.getCode());
        result.getMap().forEach((key, value) -> {
            body.set(key, toTree(value));
        });

        if (body.path("data").path("meta").has("code")) {
            // Remove this check from Version 2.1.0 onwards
            throw new IllegalArgumentException("data.meta.code, Version 2.0 RestfulApi Breaking Changes.");
        }

        return JsonUtils.toString(body);
    }

    /**
     * @param object to parse to JsonNode
     * @return parsed JsonNode
     */
    protected JsonNode toTree(Object object) {
        return JsonUtils.toTree(object);
    }

    /**
     * @see JsonResult
     * @see JsonRoute
     */
    @Override
    public String render(Object model) throws Exception {
        // Force casted to JsonResult because technically it should only return JsonResult
        return render((JsonResult) model);
    }
}
