package munch.restful.client.dynamodb;

import com.fasterxml.jackson.databind.JsonNode;
import munch.restful.client.RestfulRequest;
import munch.restful.client.RestfulResponse;
import munch.restful.core.JsonUtils;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;

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
    protected NextNodeList<T> list(String path, @Nullable Object nextHash, int size) {
        RestfulRequest request = doGet(path);
        request.queryString("size", size);

        if (nextHash != null) {
            request.queryString("next." + hashName, nextHash);
        }

        RestfulResponse response = request.asResponse();
        JsonNode next = response.getNode().path("next");

        List<T> dataList = response.asDataList(clazz);
        return new NextNodeList<>(dataList, next);
    }

    /**
     * @param path for querying via get request, e.g. /resources
     * @return Iterator of all Data
     */
    protected Iterator<T> list(String path) {
        return new Iterator<T>() {
            NextNodeList<T> nextNodeList = list(path, null, 50);
            Iterator<T> iterator = nextNodeList.iterator();

            @Override
            public boolean hasNext() {
                if (iterator.hasNext()) return true;
                if (nextNodeList.hasNext()) {
                    // Get NextNode
                    JsonNode nextNode = nextNodeList.getNext();

                    // Query list again
                    Object nextHash = JsonUtils.toObject(nextNode.path(hashName), Object.class);
                    nextNodeList = list(path, nextHash, 50);
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
    protected T get(String path, Object hash) {
        RestfulRequest request = doGet(path);
        request.path(hashName, hash);
        return request.asDataObject(clazz);
    }

    /**
     * @param path for put request, e.g. /resources/{hash}
     * @param hash value
     * @param data body value to put
     * @return Object
     */
    protected T put(String path, Object hash, T data) {
        RestfulRequest request = doPut(path);
        request.path(hashName, hash);
        request.body(data);
        return request.asDataObject(clazz);
    }

    /**
     * @param path for delete request, e.g. /resources/{hash}
     * @param hash value
     * @return Object, Null = not found
     */
    @Nullable
    protected T delete(String path, Object hash) {
        RestfulRequest request = doDelete(path);
        request.path(hashName, hash);
        return request.asDataObject(clazz);
    }
}
