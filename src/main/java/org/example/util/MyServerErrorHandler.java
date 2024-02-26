package org.example.util;

import ratpack.core.error.ServerErrorHandler;
import ratpack.core.handling.Context;

public class MyServerErrorHandler implements ServerErrorHandler {

    @Override
    public void error(Context context, Throwable throwable) {
        // Extract the error message
        String errorMessage = throwable.getMessage();

        // Handle the error and include the message in the response
        context.getResponse().status(500).send("Internal Server Error: " + errorMessage);
        throwable.printStackTrace();  // You can log the exception if needed
    }
}
