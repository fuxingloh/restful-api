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
public class RestfulDynamoHashRangeClient<T> extends RestfulDynamoClient<T> {

    protected final String rangeName;

    /**
     * @param url       base url, must not end with /
     * @param clazz     class for parsing
     * @param hashName  name of hash
     * @param rangeName name of range
     */
    public RestfulDynamoHashRangeClient(String url, Class<T> clazz, String hashName, String rangeName) {
        super(url, clazz, hashName);
        this.rangeName = rangeName;
    }

    /**
     * @param path  for querying list, e.g. /resources/{hash}
     * @param hash  value
     * @param range value
     * @param size  per list
     * @return PagedList of Data or Empty
     */
    public NextNodeList<T> list(String path, Object hash, @Nullable Object range, int size) {
        RestfulRequest request = doGet(path);
        request.path(hashName, hash);
        request.queryString("size", size);

        if (range != null) {
            request.queryString(rangeName, range);
        }

        RestfulResponse response = request.asResponse();
        JsonNode next = response.getNode().path("next");

        List<T> dataList = response.asDataList(clazz);
        return new NextNodeList<>(dataList, next);
    }

    /**
     * @param path  for querying list, e.g. /resources/{hash}
     * @param hash value
     * @param size default size per list
     * @return Iterator of all Data with the same hash
     */
    public Iterator<T> list(String path, Object hash, int size) {
        return new Iterator<T>() {
            NextNodeList<T> nextNodeList = list(path, hash, null, size);
            Iterator<T> iterator = nextNodeList.iterator();

            @Override
            public boolean hasNext() {
                if (iterator.hasNext()) return true;
                if (nextNodeList.hasNext()) {
                    // Get RangeNode Value
                    JsonNode rangeNode = nextNodeList.getNext().path(rangeName);
                    Object object = JsonUtils.toObject(rangeNode, Object.class);

                    // Query list again
                    nextNodeList = list(path, hash, object, size);
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
     * @param path  for get request, e.g. /resources/{hash}/resources/{range}
     * @param hash  value
     * @param range value
     * @return Object, Null = not found
     */
    @Nullable
    public T get(String path, Object hash, Object range) {
        RestfulRequest request = doGet(path);
        request.path(hashName, hash);
        request.path(rangeName, range);
        return request.asDataObject(clazz);
    }

    /**
     * @param path  for put request, e.g. /resources/{hash}/resources/{range}
     * @param hash  value
     * @param range value
     * @param data  body value to put
     * @return Object
     */
    public T put(String path, Object hash, Object range, T data) {
        RestfulRequest request = doPut(path);
        request.path(hashName, hash);
        request.path(rangeName, range);
        request.body(data);
        return request.asDataObject(clazz);
    }

    /**
     * @param path  for delete request, e.g. /resources/{hash}/resources/{range}
     * @param hash  value
     * @param range value
     * @return Object, Null = not found
     */
    @Nullable
    public T delete(String path, Object hash, Object range) {
        RestfulRequest request = doDelete(path);
        request.path(hashName, hash);
        request.path(rangeName, range);
        return request.asDataObject(clazz);
    }
}
