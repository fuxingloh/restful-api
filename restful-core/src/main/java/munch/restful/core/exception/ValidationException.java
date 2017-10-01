package munch.restful.core.exception;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;

/**
 * Created By: Fuxing Loh
 * Date: 5/7/2017
 * Time: 7:49 PM
 * Project: munch-core
 */
public class ValidationException extends StructuredException {

    public ValidationException(String key) {
        super(400, "ValidationException", "Validation failed on " + key);
    }

    public ValidationException(String key, String reason) {
        super(400, "ValidationException", "Validation failed on " + key + ", reason: " + reason + ".");
    }

    /**
     * @param node check if node is not null or missing
     * @return same node
     * @throws ValidationException if null or missing
     */
    public static JsonNode require(String key, JsonNode node) throws ValidationException {
        if (node.isNull() && node.isMissingNode()) {
            throw new ValidationException(key, "Required node is missing or null");
        }
        return node;
    }

    /**
     * @param node check if node is not null, missing or blank
     * @return same node
     * @throws ValidationException if null, missing or blank
     */
    public static String requireNonBlank(String key, JsonNode node) throws ValidationException {
        require(key, node);
        String text = node.asText(null);
        if (StringUtils.isBlank(text)) throw new ValidationException(key, "Required node is blank");
        return text;
    }

    /**
     * Helper method to check and get values
     *
     * @param key   key to param
     * @param value value that is required non null
     * @return same value
     * @throws ParamException if null
     */
    public static <T> T requireNonNull(String key, T value) throws ParamException {
        if (value == null) throw new ValidationException(key, "Value cannot be null");
        return value;
    }

    /**
     * Helper method to check and get values
     *
     * @param key   key to param
     * @param value value that is required non blank
     * @return same value
     * @throws ParamException if blank
     */
    public static <T extends CharSequence> T requireNonBlank(String key, T value) throws ParamException {
        if (StringUtils.isBlank(value)) throw new ValidationException(key, "Value cannot be blank");
        return value;
    }
}
