package com.example.uniqueiddemo;

import android.view.View;
import android.widget.EditText;

import androidx.recyclerview.widget.RecyclerView;

import okhttp3.OkHttpClient;

public class SymmVerifProcess implements Runnable{

    private int code;
    private VerifyMessage verifyMessage;
    private RecyclerView msgList;
    private EditText ip;
    private OkHttpClient client;
    public SymmVerifProcess(View verifyView){
        ip = verifyView.findViewById(R.id.ip_addr_verif);
        client = new OkHttpClient();
    }

    @Override
    public void run() {
        String url = "http://" + ip.getText().toString() + ":3001/api/msg_and_salt";

    }

    public int getCode(){
        return code;
    }

}
