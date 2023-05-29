package com.example.uniqueiddemo;

import android.media.MediaDrm;
import android.view.View;
import android.view.textclassifier.TextLinks;
import android.widget.EditText;

import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.security.MessageDigest;
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

    final protected static char[] hexArray = "0123456789abcdef".toCharArray();
    private static final UUID WIDEVINE_UUID = new UUID(-0x121074568629b532L, -0x5c37d8232ae2de13L);
    private int code;
    private VerifyMessage verifyMessage;
    private RecyclerView msgList;
    private EditText ip;
    private OkHttpClient client;
    private Gson gson;
    private ArrayList<VerifyMessage> messageList;
    private ArrayList<ServerCheck> sendMessage;
    private MediaDrm wvDrm;
    String kDigest;
    byte[] digest;
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
            messageList = gson.fromJson(response.body().charStream(),messageListType);
            wvDrm = new MediaDrm(WIDEVINE_UUID);
            String id = bytesToHex(wvDrm.getPropertyByteArray(MediaDrm.PROPERTY_DEVICE_UNIQUE_ID));
            int nIteration = 1000;
            int keyLength = 256;
            for (VerifyMessage message : messageList){

                PBEKeySpec pbKeySpec = new PBEKeySpec(id.toCharArray(), hexStringToByteArray(message.getSalt()), nIteration, keyLength);
                SecretKeyFactory secretKeyFactory;
                SecretKey keyBytes;
                secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
                keyBytes = secretKeyFactory.generateSecret(pbKeySpec);
                Mac hmac = Mac.getInstance("HmacSHA1");
                hmac.init(keyBytes);
                kDigest = bytesToHex(hmac.doFinal(hexStringToByteArray(message.getHashMsg())));
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

    public int getCode(){
        return code;
    }

}
