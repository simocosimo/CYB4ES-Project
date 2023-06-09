package com.example.uniqueiddemo;

public class VerifyMessage {

    private String id;
    private String hashMsg;
    private String salt;
    private String DRM;
    private String ICCID;

    public VerifyMessage(String id, String hashMsg, String salt, String DRM, String ICCID) {
        this.id = id;
        this.hashMsg = hashMsg;
        this.salt = salt;
        this.DRM = DRM;
        this.ICCID = ICCID;
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

    public String getICCID() {
        return ICCID;
    }
}
