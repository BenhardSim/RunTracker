package org.example.util;

import ratpack.core.error.ClientErrorHandler;
import ratpack.core.handling.Context;

public class MyClientErrorHandler implements ClientErrorHandler {
    @Override
    public void error(Context ctx, int statusCode) throws Exception {
        error(ctx, statusCode, null);
    }
    public void error(Context ctx, int statusCode, Throwable throwable) throws Exception {
        String errorMessage = throwable != null ? throwable.getMessage() : "An error occurred";
        String message = switch (statusCode) {
            case 404 -> "Resource not found: " + errorMessage;
            case 400 -> "Bad request: " + errorMessage;
            case 405 -> "Method not allowed: " + errorMessage;
            default -> "Client error: " + errorMessage;
        };
        ctx.getResponse().status(statusCode).send(message);
    }


}
