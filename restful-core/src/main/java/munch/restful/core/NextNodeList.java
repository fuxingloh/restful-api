package munch.restful.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;

/**
 * This is a special class that will be automatically converted in JsonResult with data & next node info
 * <p>
 * Created by: Fuxing
 * Date: 3/5/18
 * Time: 3:04 PM
 * Project: munch-partners
 */
public class NextNodeList<T> extends ArrayList<T> {

    private JsonNode next;

    /**
     * NextNodeList without next
     *
     * @param c collection to copy over
     */
    public NextNodeList(Collection<? extends T> c) {
        super(c);
    }

    /**
     * @param c    collection to copy over
     * @param next node
     */
    public NextNodeList(Collection<? extends T> c, JsonNode next) {
        super(c);
        this.next = next;
    }

    /**
     * @param c    collection to copy over
     * @param next node in map, will be converted to JsonNode
     */
    public NextNodeList(Collection<? extends T> c, Map<String, Object> next) {
        super(c);
        this.next = JsonUtils.toTree(next);
    }

    /**
     * @param c      collection to copy over
     * @param key    of next
     * @param object of next, nullable
     */
    public NextNodeList(Collection<? extends T> c, String key, @Nullable Object object) {
        super(c);
        if (object != null) {
            this.next = JsonUtils.toTree(Map.of(key, object));
        }
    }

    /**
     * @return next node
     */
    @JsonIgnore
    public JsonNode getNext() {
        return next;
    }

    /**
     * @param key          of value
     * @param defaultValue to return, Nullable
     * @return found or defaultValue
     */
    @Nullable
    public String getNextString(String key, @Nullable String defaultValue) {
        return getNext().path(key).asText(defaultValue);
    }

    /**
     * @param key          of value
     * @param defaultValue to return, Nullable
     * @return found or defaultValue
     */
    @Nullable
    public Long getNextLong(String key, @Nullable Long defaultValue) {
        JsonNode value = getNext().path(key);
        if (!value.isMissingNode() && value.isLong()) return value.asLong();
        return defaultValue;
    }

    /**
     * @param key          of value
     * @param defaultValue to return, Nullable
     * @return found or defaultValue
     */
    @Nullable
    public Integer getNextInt(String key, @Nullable Integer defaultValue) {
        JsonNode value = getNext().path(key);
        if (!value.isMissingNode() && value.isInt()) return value.asInt();
        return defaultValue;
    }

    /**
     * @return if have next node
     */
    @JsonIgnore
    public boolean hasNext() {
        return next != null && !next.isMissingNode();
    }

    /**
     * @param function using current NextNode to get the Next List
     * @return Iterator of NextNodeList Chaining
     */
    public Iterator<T> toIterator(Function<JsonNode, NextNodeList<T>> function) {
        NextNodeList<T> initial = this;
        return new Iterator<>() {
            NextNodeList<T> current = initial;
            Iterator<T> iterator = initial.iterator();

            @Override
            public boolean hasNext() {
                if (iterator.hasNext()) return true;

                if (!current.hasNext()) return false;

                current = function.apply(current.getNext());
                if (current == null) return false;
                if (current.isEmpty()) return false;

                iterator = current.iterator();
                return iterator.hasNext();
            }

            @Override
            public T next() {
                return iterator.next();
            }
        };
    }
}
