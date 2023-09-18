package com.dws.challenge.domain;

public class TransferResponse {
    private boolean success;
    private String message;

    private TransferResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public static TransferResponse success(String message) {
        return new TransferResponse(true, message);
    }

    public static TransferResponse failure(String message) {
        return new TransferResponse(false, message);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}

