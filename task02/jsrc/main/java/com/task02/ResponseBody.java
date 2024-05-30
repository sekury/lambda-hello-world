package com.task02;

public final class ResponseBody {
    private final int statusCode;
    private final String message;

    public ResponseBody(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getMessage() {
        return message;
    }
}
