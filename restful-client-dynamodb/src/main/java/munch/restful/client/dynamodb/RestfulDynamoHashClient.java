package munch.restful.client.dynamodb;

import com.fasterxml.jackson.databind.JsonNode;
import munch.restful.client.RestfulRequest;
import munch.restful.core.JsonUtils;
import munch.restful.core.NextNodeList;

import javax.annotation.Nullable;
import java.util.Iterator;

/**
 * Created by: Fuxing
 * Date: 3/5/18
 * Time: 2:43 PM
 * Project: munch-partners
 */
public class RestfulDynamoHashClient<T> extends RestfulDynamoClient<T> {

    /**
     * @param url      base url, must not end with /
     * @param clazz    class for parsing
     * @param hashName name of hash
     */
    protected RestfulDynamoHashClient(String url, Class<T> clazz, String hashName) {
        super(url, clazz, hashName);
    }

    /**
     * @param path     for querying via get request, e.g. /resources
     * @param nextHash next hash, aka: the exclusive start hash key
     * @param size     size per list
     * @return List of data with next node
     */
    protected NextNodeList<T> doList(String path, @Nullable Object nextHash, int size) {
        RestfulRequest request = doGet(path);
        request.queryString("size", size);

        if (nextHash != null) {
            request.queryString("next." + hashName, nextHash);
        }

        return request.asNextNodeList(clazz);
    }

    /**
     * @param path for querying via get request, e.g. /resources
     * @param size for HashClient querying
     * @return Iterator of all Data
     */
    protected Iterator<T> doIterator(String path, int size) {
        return new Iterator<T>() {
            NextNodeList<T> nextNodeList = doList(path, null, size);
            Iterator<T> iterator = nextNodeList.iterator();

            @Override
            public boolean hasNext() {
                if (iterator.hasNext()) return true;
                if (nextNodeList.hasNext()) {
                    // Get NextNode
                    JsonNode nextNode = nextNodeList.getNext();

                    // Query list again
                    Object nextHash = JsonUtils.toObject(nextNode.path(hashName), Object.class);
                    nextNodeList = doList(path, nextHash, size);
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

    /**
     * @param path for get request, e.g. /resources/{hash}
     * @param hash value
     * @return Object, Null = not found
     */
    @Nullable
    protected T doGet(String path, Object hash) {
        RestfulRequest request = doGet(path);
        request.path(hashName, hash);
        return request.asDataObject(clazz);
    }

    /**
     * @param path for put request, e.g. /resources/{hash}
     * @param hash value
     * @param data body value to put
     */
    protected void doPut(String path, Object hash, T data) {
        RestfulRequest request = doPut(path);
        request.path(hashName, hash);
        request.body(data);
        request.hasCode(200);
    }

    /**
     * @param path for delete request, e.g. /resources/{hash}
     * @param hash value
     * @return Object, Null = not found
     */
    @Nullable
    protected T doDelete(String path, Object hash) {
        RestfulRequest request = doDelete(path);
        request.path(hashName, hash);
        return request.asDataObject(clazz);
    }
}
