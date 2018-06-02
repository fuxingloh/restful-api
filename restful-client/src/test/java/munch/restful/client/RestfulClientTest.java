package munch.restful.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by: Fuxing
 * Date: 3/6/18
 * Time: 2:12 AM
 * Project: restful-api
 */
class RestfulClientTest {

    RestfulClient client = new RestfulClient("http://domain") {
    };

    @Test
    void ending() {
        RestfulRequest request = client.doGet("/pattern/:what");
        assertEquals(request.request.getUrl(), "http://domain/pattern/{what}");
    }

    @Test
    void starting() {
        RestfulRequest request = client.doGet("/:what/pattern");
        assertEquals(request.request.getUrl(), "http://domain/{what}/pattern");
    }

    @Test
    void middle() {
        RestfulRequest request = client.doGet("/pattern/:middle/what");
        assertEquals(request.request.getUrl(), "http://domain/pattern/{middle}/what");
    }
}