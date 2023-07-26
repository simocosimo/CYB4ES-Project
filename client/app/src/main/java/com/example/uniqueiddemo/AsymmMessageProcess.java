package com.example.uniqueiddemo;

import static com.example.uniqueiddemo.MainActivity.modulus;
import static com.example.uniqueiddemo.MainActivity.pubExponent;
import static com.example.uniqueiddemo.MainActivity.serialNumber;

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

public class AsymmMessageProcess implements Runnable{

    private final TextView ip;
    private final TextView msg;
    private final Gson gson;
    private final OkHttpClient client;
    private AsymmAddMessage addMessage;
    private int code;

    private ResponseBody res_body;

    public AsymmMessageProcess(View authView){
        ip = authView.findViewById(R.id.insert_ip);
        msg = authView.findViewById(R.id.insert_message);
        gson = new Gson();
        client = new OkHttpClient();
    }

    public void run() {

        if(ip.getText().toString().equals("") || msg.getText().toString().equals("")){
            code = 0;
            return;
        }

        String url = "http://" + ip.getText().toString() + ":3001/api/asymm/add_elements";
        MediaType JSON = MediaType.get("application/json; charset=utf-8");

        String signature = AsymmHandshakeHandler.signMessage(msg.getText().toString());

        addMessage = new AsymmAddMessage(msg.getText().toString(), signature, serialNumber);
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
