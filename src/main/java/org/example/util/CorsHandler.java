package org.example.util;

import ratpack.core.handling.Context;
import ratpack.core.handling.Handler;
import ratpack.core.http.MutableHeaders;

public class CorsHandler implements Handler {

    @Override
    public void handle(Context ctx) throws Exception {
        MutableHeaders headers = ctx.getResponse().getHeaders();

        // Allow specific origins
        headers.add("Access-Control-Allow-Origin", "*");

        // Allow specific HTTP methods
        headers.add("Access-Control-Allow-Methods", "GET, POST, OPTIONS, DELETE, PUT");

        // Allow specific headers
        headers.add("Access-Control-Allow-Headers", "Content-Type");

        // Allow credentials (if needed)
        headers.add("Access-Control-Allow-Credentials", "true");

        // Set the maximum age for preflight requests
        headers.add("Access-Control-Max-Age", "86400");

        // Continue with the next handler
        ctx.next();
    }
}

