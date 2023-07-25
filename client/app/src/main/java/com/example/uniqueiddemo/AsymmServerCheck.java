package com.example.uniqueiddemo;

public class AsymmServerCheck {

    private String hash_msg;
    private String signature_msg;
    private int id_msg;

    public String getHash() {
        return hash_msg;
    }

    public String getSignature() {
        return signature_msg;
    }

    public int getId() {
        return id_msg;
    }

    public AsymmServerCheck(String hash, int id, String signature) {
        this.hash_msg = hash;
        this.id_msg = id;
        this.signature_msg = signature;
    }
}
