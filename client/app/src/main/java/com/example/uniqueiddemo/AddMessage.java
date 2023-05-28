package com.example.uniqueiddemo;

public class AddMessage {
    String hmac;
    String hashMsg;
    String message;
    String salt;

    public AddMessage(String hmac, String hashMsg, String message, String salt){
        this.hmac = hmac;
        this.hashMsg = hashMsg;
        this.message = message;
        this.salt = salt;
    }
}
