package com.example.uniqueiddemo;

public class AddMessage {
    String hmac;
    String hashMsg;
    String message;
    String salt;
    String DRM;
    String ICCID;

    public AddMessage(String hmac, String hashMsg, String message, String salt,String drm,String iccid){
        this.hmac = hmac;
        this.hashMsg = hashMsg;
        this.message = message;
        this.salt = salt;
        this.DRM = drm;
        this.ICCID = iccid;
    }
}
