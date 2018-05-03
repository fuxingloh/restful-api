package munch.restful.client.dynamodb;

import com.fasterxml.jackson.databind.JsonNode;
import munch.restful.client.RestfulClient;
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
public class RestfulDynamoHashRangeClient<T> extends RestfulClient {

    protected final Class<T> clazz;
    protected final String hashName;
    protected final String rangeName;
    protected final String path;

    /**
     * @param url       base url
     * @param clazz     class for parsing
     * @param path      path of data service, e.g. /account/{hashName}/data/{rangeName}
     * @param hashName  name of hash
     * @param rangeName name of range
     */
    public RestfulDynamoHashRangeClient(String url, Class<T> clazz,
                                        String path, String hashName, String rangeName) {
        super(url);
        this.clazz = clazz;
        this.path = path;
        this.hashName = hashName;
        this.rangeName = rangeName;
    }

    /**
     * @param hash  value
     * @param range value
     * @param size  per list
     * @return PagedList of Data or Empty
     */
    public PagedList<T> list(Object hash, @Nullable Object range, int size) {
        RestfulRequest request = doGet(path);
        request.path(hashName, hash);
        request.path(rangeName, "list");
        request.queryString("size", size);

        if (range != null) {
            request.queryString(rangeName, range);
        }
        RestfulResponse response = request.asResponse();
        JsonNode next = response.getNode().path("next");

        List<T> dataList = response.asDataList(clazz);
        return new PagedList<>(dataList, next);
    }

    /**
     * @param hash value
     * @return Iterator of all Data with the same hash
     */
    public Iterator<T> list(Object hash) {
        return new Iterator<T>() {
            PagedList<T> pagedList = list(hash, null, 50);
            Iterator<T> iterator = pagedList.iterator();

            @Override
            public boolean hasNext() {
                if (iterator.hasNext()) return true;
                if (pagedList.hasNext()) {
                    // Get RangeNode Value
                    JsonNode rangeNode = pagedList.getNext().path(rangeName);
                    Object object = JsonUtils.toObject(rangeNode, Object.class);

                    // Query list again
                    pagedList = list(hash, object, 50);
                    iterator = pagedList.iterator();
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
     * @param hash  value
     * @param range value
     * @return Object, Null = not found
     */
    @Nullable
    public T get(Object hash, Object range) {
        RestfulRequest request = doGet(path);
        request.path(hashName, hash);
        request.path(rangeName, range);
        return request.asDataObject(clazz);
    }

    /**
     * @param hash  value
     * @param range value
     * @param data  body value to put
     * @return Object
     */
    public T put(Object hash, Object range, T data) {
        RestfulRequest request = doPut(path);
        request.path(hashName, hash);
        request.path(rangeName, range);
        request.body(data);
        return request.asDataObject(clazz);
    }

    /**
     * @param hash  value
     * @param range value
     * @return Object, Null = not found
     */
    @Nullable
    public T delete(Object hash, Object range) {
        RestfulRequest request = doDelete(path);
        request.path(hashName, hash);
        request.path(rangeName, range);
        return request.asDataObject(clazz);
    }
}
