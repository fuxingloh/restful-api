package munch.restful.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.List;

/**
 * Minimum required from this meta is only code
 * <p>
 * Created By: Fuxing Loh
 * Date: 16/6/2017
 * Time: 3:07 PM
 * Project: munch-core
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public final class RestfulMeta {

    private int code;
    private Error error;

    /**
     * This are http codes also.
     * Common codes are:
     * 1. 200 = success
     * 2. 400 = parameters error
     * 3. 404 = endpoint or object not found
     * 4. 500 = service error, unknown/known error, coding error
     *
     * @return status code
     */
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    /**
     * @return error details
     */
    public Error getError() {
        return error;
    }

    public void setError(Error error) {
        this.error = error;
    }

    public final static class Error {
        private String type;
        private String message;
        private String stacktrace;

        private List<String> sources;

        /**
         * @return error type, aka ClassType
         */
        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        /**
         * @return human readable message
         */
        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        /**
         * @return full stacktrace of error
         */
        public String getStacktrace() {
            return stacktrace;
        }

        public void setStacktrace(String stacktrace) {
            this.stacktrace = stacktrace;
        }

        /**
         * Last in list is the original source
         *
         * @return url source of error
         */
        public List<String> getSources() {
            return sources;
        }

        public void setSources(List<String> sources) {
            this.sources = sources;
        }
    }

    @JsonIgnore
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Restful meta builder class not POJO
     */
    @JsonIgnoreType
    public static class Builder {

        private RestfulMeta meta;

        private Builder() {
            meta = new RestfulMeta();
        }

        public Builder code(int code) {
            meta.setCode(code);
            return this;
        }

        public Builder errorType(String type) {
            if (meta.getError() == null) meta.setError(new Error());
            meta.getError().setType(type);
            return this;
        }

        public Builder errorMessage(String message) {
            if (meta.getError() == null) meta.setError(new Error());
            meta.getError().setMessage(message);
            return this;
        }

        public Builder errorStacktrace(Throwable stacktrace) {
            if (meta.getError() == null) meta.setError(new Error());
            meta.getError().setStacktrace(ExceptionUtils.getStackTrace(stacktrace));
            return this;
        }

        public RestfulMeta build() {
            return meta;
        }
    }
}
