package com.example.uniqueiddemo;

import static com.example.uniqueiddemo.MainActivity.modulus;
import static com.example.uniqueiddemo.MainActivity.pubExponent;

import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class AsymmKeyGenProcess implements Runnable{

    private HandshakeMessage addMessage;
//    private Switch useIccid;
    private int code;

    private ResponseBody res_body;

    public AsymmKeyGenProcess(){
    }

    public void run() {
        AsymmHandshakeHandler.keyGen();
    }

}
