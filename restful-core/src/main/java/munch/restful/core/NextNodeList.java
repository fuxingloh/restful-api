package munch.restful.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by: Fuxing
 * Date: 3/5/18
 * Time: 3:04 PM
 * Project: munch-partners
 */
public class NextNodeList<T> extends ArrayList<T> {

    private JsonNode next;

    public NextNodeList(Collection<? extends T> c, JsonNode next) {
        super(c);
        this.next = next;
    }

    /**
     * @return next node
     */
    @JsonIgnore
    public JsonNode getNext() {
        return next;
    }

    /**
     * @return if have next node
     */
    @JsonIgnore
    public boolean hasNext() {
        return next != null && !next.isMissingNode();
    }
}
