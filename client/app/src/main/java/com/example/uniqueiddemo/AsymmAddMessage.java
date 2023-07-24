package com.example.uniqueiddemo;

public class AsymmAddMessage {
    String msg;
    String signature_msg;
    int serialNumber;

    public AsymmAddMessage(String msg, String signature_msg, int serialNumber){
        this.msg = msg;
        this.signature_msg = signature_msg;
        this.serialNumber = serialNumber;
    }
}
