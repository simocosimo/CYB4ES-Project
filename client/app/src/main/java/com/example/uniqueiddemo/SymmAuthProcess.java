package com.example.uniqueiddemo;

import android.media.MediaDrm;
import android.media.UnsupportedSchemeException;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
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

public class SymmAuthProcess implements Runnable{

    private static final UUID WIDEVINE_UUID = new UUID(-0x121074568629b532L, -0x5c37d8232ae2de13L);
    private final TextView ip;
    private final TextView msg;
    private final SecureRandom secureRandom;
    private final Gson gson;
    private final OkHttpClient client;
    private MediaDrm wvDrm;
    private AddMessage addMessage;
    private final ConversionUtil conversionUtil;
    private int code;

    public SymmAuthProcess(View authView){
        ip = authView.findViewById(R.id.insert_ip);
        msg = authView.findViewById(R.id.insert_message);
        secureRandom = new SecureRandom();
        gson = new Gson();
        client = new OkHttpClient();
        conversionUtil = new ConversionUtil();
    }

    public void run() {

        if(msg.getText().toString().equals("") || ip.getText().toString().equals("")){
            code = 0;
            return;
        }
        String url = "http://" + ip.getText().toString() + ":3001/api/add_elements";
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        byte[] saltBytes = new byte[8];
        secureRandom.nextBytes(saltBytes);
        String salt = conversionUtil.bytesToHex(saltBytes);
        String hashable = msg.getText().toString() + salt;
        int nIteration = 1000;
        int keyLength = 256;

        try {
            wvDrm = new MediaDrm(WIDEVINE_UUID);
        } catch (UnsupportedSchemeException e) {
            throw new RuntimeException(e);
        }


        String id = conversionUtil.bytesToHex(wvDrm.getPropertyByteArray(MediaDrm.PROPERTY_DEVICE_UNIQUE_ID));
        PBEKeySpec pbKeySpec = new PBEKeySpec(id.toCharArray(), saltBytes, nIteration, keyLength);
        SecretKeyFactory secretKeyFactory;
        SecretKey keyBytes;
        String kDigest;
        byte[] digest;
        try {
            secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA384");
            keyBytes = secretKeyFactory.generateSecret(pbKeySpec);
            Mac hmac = Mac.getInstance("HmacSHA384");
            MessageDigest md = MessageDigest.getInstance("SHA-384");
            digest = md.digest(hashable.getBytes());
            hmac.init(keyBytes);
            kDigest = conversionUtil.bytesToHex(hmac.doFinal(digest));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException |
                 InvalidKeyException e) {
            throw new RuntimeException(e);
        }
        addMessage = new AddMessage(kDigest, conversionUtil.bytesToHex(digest), msg.getText().toString(), salt);
        String requestBody = gson.toJson(addMessage);
        try {
            RequestBody body = RequestBody.create(requestBody, JSON);
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();
            Response response = client.newCall(request).execute();
            code = response.code();
        } catch (IOException e) {
            code = 0;
        }
    }

    public int getCode(){
        return code;
    }


}
