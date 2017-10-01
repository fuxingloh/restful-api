package munch.restful.core.exception;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by: Fuxing
 * Date: 10/12/2016
 * Time: 11:16 AM
 * Project: corpus-catalyst
 */
public final class ParamException extends StructuredException {

    /**
     * Exception should be thrown for
     * Param is Blank
     * Param is Empty
     * Param is not formatted correctly (Number, Boolean)
     * Can be thrown for QueryString, PathParams
     *
     * @param param param that is not available
     */
    public ParamException(String param) {
        super(400, "ParamException", "Parameter " + param + " is required but not processable.");
    }

    /**
     * Exception should be thrown for
     * Param is Blank
     * Param is Empty
     * Param is not formatted correctly (Number, Boolean)
     * Can be thrown for QueryString, PathParams
     *
     * @param params list of param that is not available
     */
    public ParamException(String... params) {
        super(400, "ParamException", "Parameter " + String.join(", ", params) + " is required but not processable.");
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
        if (value == null) throw new ParamException(key);
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
        if (StringUtils.isBlank(value)) throw new ParamException(key);
        return value;
    }
}
