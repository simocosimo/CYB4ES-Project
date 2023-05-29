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

    final protected static char[] hexArray = "0123456789abcdef".toCharArray();
    private static final UUID WIDEVINE_UUID = new UUID(-0x121074568629b532L, -0x5c37d8232ae2de13L);
    private TextView ip;
    private TextView msg;
    private SecureRandom secureRandom;
    private Gson gson;
    private OkHttpClient client;
    private MediaDrm wvDrm;
    private AddMessage addMessage;
    private int code;

    public SymmAuthProcess(View authView){
        ip = authView.findViewById(R.id.insert_ip);
        msg = authView.findViewById(R.id.insert_message);
        secureRandom = new SecureRandom();
        gson = new Gson();
        client = new OkHttpClient();
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
        String salt = bytesToHex(saltBytes);
        String hashable = msg.getText().toString() + salt;
        int nIteration = 1000;
        int keyLength = 256;

        try {
            wvDrm = new MediaDrm(WIDEVINE_UUID);
        } catch (UnsupportedSchemeException e) {
            throw new RuntimeException(e);
        }


        String id = bytesToHex(wvDrm.getPropertyByteArray(MediaDrm.PROPERTY_DEVICE_UNIQUE_ID));
        PBEKeySpec pbKeySpec = new PBEKeySpec(id.toCharArray(), saltBytes, nIteration, keyLength);
        SecretKeyFactory secretKeyFactory;
        SecretKey keyBytes;
        String kDigest;
        byte[] digest;
        try {
            secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            keyBytes = secretKeyFactory.generateSecret(pbKeySpec);
            Mac hmac = Mac.getInstance("HmacSHA1");
            MessageDigest md = MessageDigest.getInstance("SHA-384");
            digest = md.digest(hashable.getBytes());
            hmac.init(keyBytes);
            kDigest = bytesToHex(hmac.doFinal(digest));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException |
                 InvalidKeyException e) {
            throw new RuntimeException(e);
        }
        addMessage = new AddMessage(kDigest, bytesToHex(digest), msg.getText().toString(), salt);
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
            throw new RuntimeException(e);
        }
    }

    public int getCode(){
        return code;
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte[] hexStringToByteArray(String s) {
        try {

            int len = s.length();
            if(len>1) {
                byte[] data = new byte[len / 2];
                for (int i = 0 ; i < len ; i += 2) {
                    data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                            + Character.digit(s.charAt(i + 1), 16));
                }
                return data;
            }
            else

            {
                return  null;
            }
        }catch (Exception e)
        {
            throw e;
        }
    }
}
