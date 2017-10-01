package munch.restful.core.exception;

import munch.restful.core.RestfulMeta;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by: Fuxing
 * Date: 16/6/2017
 * Time: 12:42 PM
 * Project: munch-core
 */
public class StructuredException extends RuntimeException {

    private final int code;
    private final String type;
    private final String message;

    private final String stacktrace;
    private List<String> sources = new ArrayList<>();

    public StructuredException(StructuredException e) {
        this(e.code, e.type, e.message, e.stacktrace);
        this.sources = e.sources;
    }

    /**
     * @param code    code for error
     * @param type    error type name
     * @param message error message
     */
    public StructuredException(int code, String type, String message) {
        this(code, type, message, (String) null);
    }

    /**
     * @param code      code for error
     * @param type      error type name
     * @param message   error message
     * @param throwable error stacktrace
     */
    public StructuredException(int code, String type, String message, Throwable throwable) {
        super(message, throwable);
        this.code = code;
        this.type = type;
        this.message = message;
        this.stacktrace = ExceptionUtils.getStackTrace(throwable);
    }

    /**
     * @param code       code for error
     * @param type       error type name
     * @param message    error message
     * @param stacktrace error stacktrace string
     */
    private StructuredException(int code, String type, String message, String stacktrace) {
        super(type + ": " + message);
        this.code = code;
        this.type = type;
        this.message = message;
        this.stacktrace = stacktrace;
    }

    public int getCode() {
        return code;
    }

    public String getType() {
        return type;
    }

    public List<String> getSources() {
        return sources;
    }

    /**
     * Convert Structured exception to meta to restful use
     *
     * @return RestfulMeta
     */
    public RestfulMeta toMeta() {
        RestfulMeta meta = new RestfulMeta();
        meta.setCode(code);

        RestfulMeta.Error error = new RestfulMeta.Error();
        error.setType(type);
        error.setMessage(message);
        error.setStacktrace(stacktrace);
        error.setSources(sources);
        meta.setError(error);
        return meta;
    }

    /**
     * @param types types of errors to check if exist
     * @return true = exist in meta
     */
    public boolean hasType(String... types) {
        // Loop check if type match
        for (String t : types) if (t.equals(type)) return true;
        return false;
    }

    /**
     * @param meta   meta node
     * @param source add new source
     * @return Structured exception
     * @throws NullPointerException if meta error node is null
     */
    public static StructuredException fromMeta(RestfulMeta meta, @Nullable String source) {
        RestfulMeta.Error error = meta.getError();
        StructuredException exception = new StructuredException(meta.getCode(), error.getType(),
                error.getMessage(), error.getStacktrace());

        // Pass the source down
        exception.sources.addAll(error.getSources());
        if (source != null) exception.sources.add(source);
        return exception;
    }
}
