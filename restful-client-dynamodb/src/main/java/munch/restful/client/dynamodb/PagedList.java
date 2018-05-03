package munch.restful.client.dynamodb;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by: Fuxing
 * Date: 3/5/18
 * Time: 3:04 PM
 * Project: munch-partners
 */
public class PagedList<T> extends ArrayList<T> {
    private JsonNode next;

    public PagedList(Collection<? extends T> c, JsonNode next) {
        super(c);
        this.next = next;
    }

    public JsonNode getNext() {
        return next;
    }

    public boolean hasNext() {
        return !next.isMissingNode();
    }
}
