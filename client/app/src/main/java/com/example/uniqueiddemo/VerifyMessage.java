package com.example.uniqueiddemo;

public class VerifyMessage {

    private String id;
    private String hashMsg;
    private String salt;

    public VerifyMessage(String id, String hashMsg, String salt) {
        this.id = id;
        this.hashMsg = hashMsg;
        this.salt = salt;
    }

    public int getIDmsg() {
        return Integer.parseInt(id);
    }

    public String getHashMsg() {
        return hashMsg;
    }

    public String getSalt() {
        return salt;
    }
}
