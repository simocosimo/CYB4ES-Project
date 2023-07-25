package com.example.uniqueiddemo;

public class AsymmVerifiedMessage {

    private String id_msg;
    private String msg;
    private String signature_msg;

    public AsymmVerifiedMessage(String id, String message, String signature) {
        this.id_msg = id;
        this.msg = message;
        this.signature_msg = signature;
    }

    public String getId() {
        return id_msg;
    }

    public String getMessage() {
        return msg;
    }

    public String getSignature() { return signature_msg; }

    public String getHash() {
        try {
            byte[] hash = AsymmHandshakeHandler.sha384(msg.getBytes());
            return ConversionUtil.bytesToHex(hash);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
