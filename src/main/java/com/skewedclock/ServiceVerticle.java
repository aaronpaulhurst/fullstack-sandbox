package com.skewedclock;

import io.vertx.core.*;
import io.vertx.core.http.*;
import io.vertx.core.json.*;
import io.vertx.ext.web.*;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.BodyHandler;

public class ServiceVerticle extends AbstractVerticle {

    // Which port to run on?
    private static final int PORT = 8080;

    // State: are we accepting new jobs?
    private boolean run = true;

    // Handle: GET /validate-git
    // Input:  request parameter "value"
    // Output: response JSON...
    //    { valid: Boolean }
    private void handleValidateGit(RoutingContext routingContext) {
        String obj = routingContext.request().getParam("value");
        System.out.println("    validate: "+obj);
        HttpServerResponse resp = routingContext.response();

        JsonObject data = new JsonObject();
        data.put("valid", obj.startsWith("p-"));
        resp.putHeader("content-type", "application/json; charset=utf-8");
        resp.end(Json.encodePrettily(data));
    }

    // Handle: GET /status
    // Input:  none
    // Output: response JSON...
    //    { run: Boolean }
    private void getStatus(RoutingContext routingContext) {
        System.out.println("    getStatus");

        JsonObject data = new JsonObject();
        data.put("run", run);

        HttpServerResponse resp = routingContext.response();
        resp.putHeader("content-type", "application/json; charset=utf-8");
        resp.end(Json.encodePrettily(data));
    }

    // Handle: PUT /status
    // Input:  request JSON...
    //    { run: Boolean }
    // Output: response JSON...
    //    { run: Boolean }
    private void putStatus(RoutingContext routingContext) {
        System.out.println("    putStatus: " + routingContext.getBodyAsString());

        JsonObject json = routingContext.getBodyAsJson();
        if (json == null) { return; }

        System.out.println("    "+json.toString());
        if (json.getBoolean("run") != null) {
            run = json.getBoolean("run");
        }

        JsonObject data = new JsonObject();
        data.put("run", run);

        HttpServerResponse resp = routingContext.response();
        resp.putHeader("content-type", "application/json; charset=utf-8");
        resp.end(Json.encodePrettily(data));
    }

    // Setup verticle
    @Override
    public void start(Future<Void> fut) {
        System.out.println("Running web server on port " + PORT);

        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);

        StaticHandler content = StaticHandler.create();

        // XXX: Debugging only
        // XXX: Used to allow testing of local Javascript
        router.route().handler(CorsHandler.create("*")
            // XXX: Some methods/headers need to be allowed to
            // XXX: support OPTIONS preflight
            .allowedMethod(io.vertx.core.http.HttpMethod.GET)
            .allowedMethod(io.vertx.core.http.HttpMethod.PUT)
            .allowedMethod(io.vertx.core.http.HttpMethod.POST)
            .allowedMethod(io.vertx.core.http.HttpMethod.OPTIONS)
            .allowedHeader("Access-Control-Request-Method")
            .allowedHeader("Access-Control-Allow-Method")
            .allowedHeader("Access-Control-Allow-Origin")
            .allowedHeader("Access-Control-Allow-Credentials")
            .allowedHeader("Content-Type"));

        // XXX: Disable caching for online testing
        // XXX: Used to allow Disable caching for online testing
	content.setCachingEnabled(false);

        // Serve static content
        // Anything in the static/ directory; also the top-level index.html.
        router.route("/static/*").handler(content);
        router.routeWithRegex(".*\\.html").handler(content);

        // Converts body content to JSON.
        router.route().handler(BodyHandler.create());

        // --- Begin routes ---
        router.get("/validate-git").handler(this::handleValidateGit);
        router.get("/status").handler(this::getStatus);
        router.put("/status").handler(this::putStatus);
        // --- End routes ---

        // Main request handler
        server.requestHandler(router::accept)
            .listen(PORT, result-> {
                if (result.succeeded()) {
                    fut.complete();
                } else {
                    fut.fail(result.cause());
                    System.exit(1);
                }
            });
    }
}
