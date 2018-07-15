package munch.restful.core.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by: Fuxing
 * Date: 15/7/18
 * Time: 7:38 PM
 * Project: restful-api
 */
class ExceptionParserTest {
    @Test
    void name() {
        ExceptionParser.parse(new NullPointerException());
    }
}