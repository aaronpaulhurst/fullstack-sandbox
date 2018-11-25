package com.skewedclock;

import io.vertx.core.Vertx;

public class Main {

    // Main entry point.
    // Deploy Vert.x Verticles.
    public static void main(String [] args) {
        Vertx.vertx().deployVerticle(new ServiceVerticle(), result -> {
            if (result.succeeded()) {
                System.out.println("Vert.x startup complete");
            } else {
                System.out.println("Vert.x startup failed: " + result.cause());
            }
        });
    }
}
