package com.example.uniqueiddemo;

import static com.example.uniqueiddemo.MainActivity.iccid;
import static com.example.uniqueiddemo.MainActivity.keyPair;

import android.media.MediaDrm;
import android.os.Build;
import android.view.View;
import android.widget.EditText;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AsymmVerifProcess implements Runnable{
    private static final UUID WIDEVINE_UUID = new UUID(-0x121074568629b532L, -0x5c37d8232ae2de13L);
    private int code;
    private final EditText ip;
    private final OkHttpClient client;
    private final Gson gson;
    private ArrayList<AsymmVerifyMessage> messageList;
    private ArrayList<AsymmServerCheck> verify;
    private ArrayList<AsymmVerifiedMessage> verified;

    public AsymmVerifProcess(View verifyView){
        ip = verifyView.findViewById(R.id.ip_addr_verif);
        client = new OkHttpClient();
        gson = new Gson();
        verify = new ArrayList<>();
        verified = new ArrayList<>();
    }

    @Override
    public void run() {

        if(ip.getText().toString().equals("")){
            code = 0;
            return;
        }

        String url = "http://" + ip.getText().toString() + ":3001/api/asymm/verify";
        MediaType JSON = MediaType.get("application/json; charset=utf-8");

        try{
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            Response response = client.newCall(request).execute();
            Type messageListType = new TypeToken<ArrayList<AsymmVerifyMessage>>() {}.getType();
            messageList = gson.fromJson(response.body().string(),messageListType);

            for (AsymmVerifyMessage message : messageList){
                System.out.println(message.toString());
                // TODO: get the hash from the object and encrypt it with privatekey
                String hash_msg = message.getHashMsg();
                System.out.println("Message " + message.getIDmsg() + " hash is " + hash_msg);
                String inc_msg = message.getMsg();
                String incSigned = AsymmHandshakeHandler.signMessage(inc_msg);
                String originalSigned = AsymmHandshakeHandler.signMessage("camomillo");
                System.out.println("Recalc signature is " + incSigned);
                System.out.println("Original signs is " + originalSigned);
                verify.add(new AsymmServerCheck(hash_msg, message.getIDmsg(), incSigned));
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        AsymmVerifyArray verifyArray = new AsymmVerifyArray(verify);
        String requestBody = gson.toJson(verifyArray);
        System.out.println(requestBody);

        String url1 = "http://" + ip.getText().toString() + ":3001/api/asymm/updateCheck";
        RequestBody body = RequestBody.create(requestBody,JSON);
        Response response;
        try{
            Request request = new Request.Builder()
                    .url(url1)
                    .put(body)
                    .build();
            response = client.newCall(request).execute();
            code = response.code();
            Type verifiedType = new TypeToken<ArrayList<AsymmVerifiedMessage>>() {}.getType();
            verified = gson.fromJson(response.body().string(),verifiedType);
        }
        catch (IOException e) {
            code = 100;
            throw new RuntimeException(e);
        }
    }
    public int getCode(){
        return code;
    }
    public ArrayList<AsymmVerifiedMessage> getVerified(){
        return verified;
    }

    private class AsymmVerifyArray{
        private ArrayList<AsymmServerCheck> update;

        public AsymmVerifyArray(ArrayList<AsymmServerCheck> verify) {
            this.update = verify;
        }
    }

}
