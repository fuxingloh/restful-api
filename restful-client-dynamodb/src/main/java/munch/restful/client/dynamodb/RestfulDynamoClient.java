package munch.restful.client.dynamodb;

import com.fasterxml.jackson.databind.JsonNode;
import munch.restful.client.RestfulClient;
import munch.restful.client.RestfulRequest;
import munch.restful.core.JsonUtils;
import munch.restful.core.NextNodeList;

import javax.annotation.Nullable;
import java.util.Iterator;

/**
 * Created by: Fuxing
 * Date: 6/5/18
 * Time: 2:20 PM
 * Project: restful-api
 */
public abstract class RestfulDynamoClient<T> extends RestfulClient {

    protected final Class<T> clazz;
    protected final String hashName;

    /**
     * @param url      base url, must not end with /
     * @param clazz    class for parsing
     * @param hashName primary hash name of the table
     */
    protected RestfulDynamoClient(String url, Class<T> clazz, String hashName) {
        super(url);
        this.clazz = clazz;
        this.hashName = hashName;
    }

    /**
     * @param path      for querying list, e.g. /resources/{hash}
     * @param hashName  name of hash
     * @param hash      value
     * @param rangeName name of range
     * @param nextRange value
     * @param size      per list
     * @return PagedList of Data or Empty
     */
    protected NextNodeList<T> doList(String path, String hashName, Object hash, String rangeName, @Nullable Object nextRange, int size) {
        RestfulRequest request = doGet(path);
        request.path(hashName, hash);
        request.queryString("size", size);

        if (nextRange != null) {
            request.queryString("next." + rangeName, nextRange);
        }

        return request.asNextNodeList(clazz);
    }

    /**
     * @param path      for querying list, e.g. /resources/{hash}
     * @param hashName  name of hash
     * @param hash      value
     * @param rangeName name of range
     * @param size      default size per list
     * @return Iterator of all Data with the same hash
     */
    protected Iterator<T> doIterator(String path, String hashName, Object hash, String rangeName, int size) {
        return new Iterator<>() {
            NextNodeList<T> nextNodeList = doList(path, hashName, hash, rangeName, null, size);
            Iterator<T> iterator = nextNodeList.iterator();

            @Override
            public boolean hasNext() {
                if (iterator.hasNext()) return true;
                if (nextNodeList.hasNext()) {
                    // Get RangeNode Value
                    JsonNode rangeNode = nextNodeList.getNext().path(rangeName);
                    Object object = JsonUtils.toObject(rangeNode, Object.class);

                    // Query list again
                    nextNodeList = doList(path, hashName, hash, rangeName, object, size);
                    iterator = nextNodeList.iterator();
                    return iterator.hasNext();
                }
                return false;
            }

            @Override
            public T next() {
                return iterator.next();
            }
        };
    }
}
