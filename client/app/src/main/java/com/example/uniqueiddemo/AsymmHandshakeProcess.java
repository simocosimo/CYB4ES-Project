package com.example.uniqueiddemo;

import static com.example.uniqueiddemo.MainActivity.modulus;
import static com.example.uniqueiddemo.MainActivity.pubExponent;

import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class AsymmHandshakeProcess implements Runnable{

    private final TextView ip;
    private final TextView msg;
    private final Gson gson;
    private final OkHttpClient client;
    private HandshakeMessage addMessage;
//    private Switch useIccid;
    private int code;

    private ResponseBody res_body;

    public AsymmHandshakeProcess(View authView){
        ip = authView.findViewById(R.id.insert_ip);
        msg = authView.findViewById(R.id.insert_message);
//        secureRandom = new SecureRandom();
        gson = new Gson();
        client = new OkHttpClient();
//        useIccid = authView.findViewById(R.id.use_iccid);
    }

    public void run() {

        if(ip.getText().toString().equals("")){
            code = 0;
            return;
        }
        // change this to the right endpoint of asymm
        String url = "http://" + ip.getText().toString() + ":3001/api/asymm/handshake";
        MediaType JSON = MediaType.get("application/json; charset=utf-8");

        addMessage = new HandshakeMessage(modulus, pubExponent);
        String requestBody = gson.toJson(addMessage);
        try {
            RequestBody body = RequestBody.create(requestBody, JSON);
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();
            Response response = client.newCall(request).execute();
            code = response.code();
            res_body = response.body();
        } catch (IOException e) {
            code = 0;
        }
    }

    public String getBody() {
        try {
            return res_body.string();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public int getCode(){
        return code;
    }


}