package com.example.uniqueiddemo;

public class VerifyMessage {

    private int IDmsg;
    private String hashMsg;
    private String salt;

    public VerifyMessage(int IDmsg, String hashMsg, String salt) {
        this.IDmsg = IDmsg;
        this.hashMsg = hashMsg;
        this.salt = salt;
    }
}
