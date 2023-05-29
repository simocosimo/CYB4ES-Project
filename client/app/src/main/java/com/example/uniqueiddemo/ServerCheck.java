package com.example.uniqueiddemo;

public class ServerCheck {

    private String hmac;
    private int id;

    public String getHmac() {
        return hmac;
    }

    public int getId() {
        return id;
    }

    public ServerCheck(String hmac, int id) {
        this.hmac = hmac;
        this.id = id;
    }
}
