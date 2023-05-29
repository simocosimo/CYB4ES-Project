package com.example.uniqueiddemo;

import android.media.MediaDrm;
import android.view.View;

import android.widget.EditText;

import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;

import java.lang.reflect.Type;

import java.util.ArrayList;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SymmVerifProcess implements Runnable{
    private static final UUID WIDEVINE_UUID = new UUID(-0x121074568629b532L, -0x5c37d8232ae2de13L);
    private int code;
    private RecyclerView msgList;
    private final EditText ip;
    private final OkHttpClient client;
    private final Gson gson;
    private ArrayList<VerifyMessage> messageList;
    private ArrayList<ServerCheck> sendMessage;
    private MediaDrm wvDrm;
    String kDigest;
    public SymmVerifProcess(View verifyView){
        ip = verifyView.findViewById(R.id.ip_addr_verif);
        client = new OkHttpClient();
        gson = new Gson();
        sendMessage = new ArrayList<ServerCheck>();
    }

    @Override
    public void run() {

        if(ip.getText().toString().equals("")){
            code = 0;
            return;
        }

        String url = "http://" + ip.getText().toString() + ":3001/api/msg_and_salt";
        MediaType JSON = MediaType.get("application/json; charset=utf-8");

        try{
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            Response response = client.newCall(request).execute();
            Type messageListType = new TypeToken<ArrayList<VerifyMessage>>() {}.getType();
            if(response.body() != null){
                messageList = gson.fromJson(response.body().charStream(),messageListType);
            }else{
                code = 1;
                return;
            }
            wvDrm = new MediaDrm(WIDEVINE_UUID);
            String id = ConversionUtil.bytesToHex(wvDrm.getPropertyByteArray(MediaDrm.PROPERTY_DEVICE_UNIQUE_ID));
            int nIteration = 1000;
            int keyLength = 256;
            for (VerifyMessage message : messageList){

                PBEKeySpec pbKeySpec = new PBEKeySpec(id.toCharArray(), ConversionUtil.hexStringToByteArray(message.getSalt()), nIteration, keyLength);
                SecretKeyFactory secretKeyFactory;
                SecretKey keyBytes;
                secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA384");
                keyBytes = secretKeyFactory.generateSecret(pbKeySpec);
                Mac hmac = Mac.getInstance("HmacSHA384");
                hmac.init(keyBytes);
                kDigest = ConversionUtil.bytesToHex(hmac.doFinal(ConversionUtil.hexStringToByteArray(message.getHashMsg())));
                sendMessage.add(new ServerCheck(kDigest,message.getIDmsg()));

            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        String requestBody = gson.toJson(sendMessage);

        url = "http://" + ip.getText().toString() + ":3001/api/updateCheck";
        RequestBody body = RequestBody.create(requestBody,JSON);
        Response response;
        try{
            Request request = new Request.Builder()
                    .url(url)
                    .put(body)
                    .build();
            response = client.newCall(request).execute();
            code = response.code();
        }
        catch (IOException e) {
            code = 0;
            throw new RuntimeException(e);
        }
    }
    public int getCode(){
        return code;
    }

}
