package com.example.uniqueiddemo;

public class AsymmVerifyMessage {

    private String id_msg;
    private String hash_msg;
    private String msg;

    public AsymmVerifyMessage(String id_msg, String hash_msg, String msg) {
        this.id_msg = id_msg;
        this.hash_msg = hash_msg;
        this.msg = msg;
    }

    public int getIDmsg() {
        return Integer.parseInt(id_msg);
    }

    public String getHashMsg() {
        return hash_msg;
    }

    public String getMsg() {
        return msg;
    }
}
