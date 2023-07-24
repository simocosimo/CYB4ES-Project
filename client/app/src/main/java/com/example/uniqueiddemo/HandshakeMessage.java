package com.example.uniqueiddemo;

public class HandshakeMessage {
    String n;
    String e;

    public HandshakeMessage(String modulus, String pubExponent){
        this.n = modulus;
        this.e = pubExponent;
    }
}
