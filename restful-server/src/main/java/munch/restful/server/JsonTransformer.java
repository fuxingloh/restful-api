package munch.restful.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import munch.restful.core.RestfulMeta;
import spark.ResponseTransformer;

/**
 * Created by: Fuxing
 * Date: 16/6/2017
 * Time: 1:46 PM
 * Project: munch-core
 */
public class JsonTransformer implements ResponseTransformer {
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

    private static final String Meta200String = toJson(Meta200);
    private static final String Meta404String = toJson(Meta404);

    @Override
    public String render(Object model) {
        // Check if JsonNode is any of the static helpers
        if (model == null || model == Meta404) return Meta404String;
        if (model == Meta200) return Meta200String;

        // Json node means already structured
        if (model instanceof JsonNode) return toJson(model);

        // If not json node, wrap it into data node
        ObjectNode nodes = objectMapper.createObjectNode();
        nodes.set("meta", objectMapper.createObjectNode().put("code", 200));
        nodes.set("data", objectMapper.valueToTree(model));
        return toJson(nodes);
    }

    /**
     * @param object object
     * @return object write to json string
     */
    public static String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
