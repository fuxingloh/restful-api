package munch.restful.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.ConfigFactory;
import munch.restful.core.JsonUtils;
import munch.restful.core.RestfulMeta;
import munch.restful.core.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Response;
import spark.Spark;

import java.net.SocketTimeoutException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Created by: Fuxing
 * Date: 9/12/2016
 * Time: 6:47 PM
 * Project: corpus-catalyst
 */
public class RestfulServer {
    protected static final Logger logger = LoggerFactory.getLogger(RestfulServer.class);
    protected static final ObjectMapper objectMapper = JsonService.objectMapper;
    protected static final String DEFAULT_HEALTH_PATH = "/health/check";

    protected static final Set<String> MUTED_TYPE = Set.of(
            AuthenticationException.class.getName(),
            JsonException.class.getName(),
            CodeException.class.getName(),
            LimitException.class.getName(),
            OfflineException.class.getName(),
            TimeoutException.class.getName(),
            UnavailableException.class.getName()
    );

    private final RestfulService[] routers;
    private boolean started = false;
    private boolean debug = true;

    /**
     * @param routers array of routes for spark server to route with
     */
    public RestfulServer(RestfulService... routers) {
        this.routers = routers;
    }

    /**
     * Support for guice injections
     *
     * @param routers set of routes for spark server to route with
     */
    public RestfulServer(Collection<RestfulService> routers) {
        this(routers.toArray(new RestfulService[0]));
    }

    /**
     * Start restful server with default port in the config = http.port
     * Port number can also be injected in the env as: HTTP_PORT
     *
     * @see RestfulServer#start(int)
     */
    public void start() {
        start(ConfigFactory.load().getInt("http.port"));
    }

    /**
     * Start Spark Json Server with given routers
     * Expected status code spark server should return is
     * 200: ok, no error in request
     * 400: structured error, constructed error from developer
     * 500: unknown error, all exception
     * 404: not found, endpoint not found
     * <p>
     * body: always json
     * <pre>
     * {
     *     meta: {code: 200},
     *     data: {name: "Explicit data body"},
     *     "other": {name: "Other implicit data body"}
     * }
     * </pre>
     *
     * @param port port to run server with
     */
    public void start(int port) {
        // Setup port
        Spark.port(port);

        // Logging Setup
        logger.info("Path logging is registered to trace.");
        // Because it is trace, to activate logging
        // set munch.restful.server.RestfulServer to trace
        Spark.before((request, response) -> {
            if (!request.pathInfo().equals(DEFAULT_HEALTH_PATH)) {
                logger.trace("{}: {}", request.requestMethod(), request.pathInfo());
            }
        });

        // Setup all routers
        setupRouters();

        // Default handler for not found
        Spark.notFound((req, res) -> {
            res.header("content-type", JsonRoute.APP_JSON);

            String path = req.pathInfo();
            return JsonUtils.toString(objectMapper.createObjectNode()
                    .set("meta", objectMapper.valueToTree(RestfulMeta.builder()
                            .code(404)
                            .errorType("EndpointNotFound")
                            .errorMessage("Requested " + path + " endpoint is not registered.")
                            .build())));
        });
        logger.info("Registered http 404 not found json response.");

        // Handle all expected exceptions
        handleException();
        logger.info("Started Spark Server on port: {}", port);
        this.started = true;
    }

    /**
     * Setup all the routers by starting them
     */
    protected void setupRouters() {
        for (RestfulService router : routers) {
            router.start();
            logger.info("Started SparkRouter: {}", router.getClass().getSimpleName());
        }
    }

    /**
     * @see StructuredException to see values and creating custom structured exception
     * @see UnknownException to see how unknown exception values is mapped
     * @see RestfulMeta to see how it is formatted
     */
    protected void handleException() {
        logger.info("Adding exception handling for CodeException.");
        Spark.exception(CodeException.class, (exception, request, response) -> {
            int code = ((CodeException) exception).getCode();
            try {
                response.status(code);
                ObjectNode nodes = objectMapper.createObjectNode();
                nodes.putObject("meta").put("code", code);
                response.body(objectMapper.writeValueAsString(nodes));
                response.type(JsonRoute.APP_JSON);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });

        logger.info("Adding exception handling for StructuredException.");
        Spark.exception(StructuredException.class, (exception, request, response) -> {
            List<String> sources = ((StructuredException) exception).getSources();
            logger.warn("Structured exception thrown from sources: {}", sources, exception);
            handleException(new JsonCall(request, response), (StructuredException) exception);
        });

        logger.info("Adding exception handling for TimeoutException.");
        Spark.exception(SocketTimeoutException.class, (exception, request, response) -> {
            handleException(new JsonCall(request, response), new TimeoutException(exception));
        });

        logger.info("Adding exception handling for all Exception.");
        Spark.exception(Exception.class, (exception, request, response) -> {
            try {
                if (mapException(exception)) return;
                // Unknown exception
                logger.warn("Unknown exception thrown", exception);
                handleException(new JsonCall(request, response), new UnknownException(exception));
            } catch (StructuredException structured) {
                // Mapped exception
                logger.warn("Structured exception thrown", exception);
                handleException(new JsonCall(request, response), structured);
            }
        });
    }

    /**
     * If mapped, you can throw it
     *
     * @param exception additional exception to map
     * @return whether it is handled, meaning not to logg
     * @throws StructuredException mapped exceptions
     */
    protected boolean mapException(Exception exception) throws StructuredException {
        return false;
    }

    /**
     * @param call      with request & response
     * @param exception exception to write
     */
    protected void handleException(JsonCall call, StructuredException exception) {
        try {
            Response response = call.response();
            response.status(exception.getCode());
            RestfulMeta restfulMeta = exception.toMeta();
            if (!debug && restfulMeta.getError() != null) {
                // If debug mode is disabled, stacktrace and source will be removed
                // Exception will still be logged
                restfulMeta.getError().setStacktrace(null);
                restfulMeta.getError().setSources(null);

                // Mute message for theses error type
                if (MUTED_TYPE.contains(restfulMeta.getError().getType())) {
                    restfulMeta.getError().setMessage(null);
                }
            }

            ObjectNode metaNode = objectMapper.valueToTree(restfulMeta);
            JsonNode nodes = objectMapper.createObjectNode().set("meta", metaNode);
            response.body(objectMapper.writeValueAsString(nodes));
            response.type(JsonRoute.APP_JSON);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return true if restful server has started
     */
    public boolean isStarted() {
        return started;
    }

    /**
     * @return whether it is debug mode
     */
    public boolean isDebug() {
        return debug;
    }

    /**
     * If debug mode is true, stacktrace and sources will be remove for RestfulMeta
     * Otherwise,
     *
     * @param debug debug mode
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    /**
     * @return port
     * @throws IllegalStateException when the server is not started
     */
    public int getPort() {
        return Spark.port();
    }

    /**
     * Using default /health/check as path
     *
     * @return RestfulServer
     */
    public RestfulServer withHealth() {
        return withHealth(DEFAULT_HEALTH_PATH);
    }

    /**
     * @param check function to reply with
     * @return RestfulServer
     */
    public RestfulServer withHealth(Function<JsonCall, String> check) {
        return withHealth(DEFAULT_HEALTH_PATH, check);
    }

    /**
     * @param runnable run a task, if task completes or fail, health will fail too
     * @return RestfulServer
     */
    public RestfulServer withHealth(Runnable runnable) {
        CompletableFuture<Void> future = CompletableFuture.runAsync(runnable);

        return withHealth(DEFAULT_HEALTH_PATH, call -> {
            if (future.isDone()) {
                throw new CodeException(400);
            }
            return JsonTransformer.Meta200String;
        });
    }

    /**
     * @param path for the health check
     * @return RestfulServer
     */
    public RestfulServer withHealth(String path) {
        return withHealth(path, call -> JsonTransformer.Meta200String);
    }

    /**
     * @param path  for the health check
     * @param check function to reply with
     * @return RestfulServer
     */
    public RestfulServer withHealth(String path, Function<JsonCall, String> check) {
        Spark.get(path, (req, res) -> check.apply(new JsonCall(req, res)));
        return this;
    }

    /**
     * Easy way to start a service in a server with the default port
     * <p>
     * Start restful server with default port in the config = http.port
     * Port number can also be injected in the env as: HTTP_PORT
     *
     * @param services to start
     * @return started RestfulServer
     */
    public static RestfulServer start(RestfulService... services) {
        return start("", services);
    }

    /**
     * Easy way to start a service in a server with the prefix path and default port
     * <p>
     * Start restful server with default port in the config = http.port
     * Port number can also be injected in the env as: HTTP_PORT
     *
     * @param prefixPath path prefix, e.g. version number
     * @param services   to start
     * @return started RestfulServer
     */
    public static RestfulServer start(String prefixPath, RestfulService... services) {
        RestfulServer server = new RestfulServer(services) {
            @Override
            protected void setupRouters() {
                Spark.path(prefixPath, super::setupRouters);
            }
        };
        server.start();
        return server;
    }


    /**
     * Easy way to start a service in a server with the prefix path and default port
     * <p>
     * Start restful server with default port in the config = http.port
     * Port number can also be injected in the env as: HTTP_PORT
     *
     * @param port       to start service in
     * @param prefixPath path prefix, e.g. version number
     * @param services   to start
     * @return started RestfulServer
     */
    public static RestfulServer start(int port, String prefixPath, RestfulService... services) {
        RestfulServer server = new RestfulServer(services) {
            @Override
            protected void setupRouters() {
                Spark.path(prefixPath, super::setupRouters);
            }
        };
        server.start(port);
        return server;
    }
}
