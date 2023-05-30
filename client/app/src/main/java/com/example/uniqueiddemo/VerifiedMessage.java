package com.example.uniqueiddemo;

public class VerifiedMessage {

    private String id;
    private String message;
    private String hash;

    public VerifiedMessage(String id, String message, String hash) {
        this.id = id;
        this.message = message;
        this.hash = hash;
    }

    public String getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public String getHash() {
        return hash;
    }
}
