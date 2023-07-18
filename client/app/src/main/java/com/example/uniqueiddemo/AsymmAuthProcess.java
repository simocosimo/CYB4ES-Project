//package com.example.uniqueiddemo;
//
//import static com.example.uniqueiddemo.MainActivity.iccid;
//
//import android.media.MediaDrm;
//import android.media.UnsupportedSchemeException;
//import android.os.Build;
//import android.view.View;
//import android.widget.Switch;
//import android.widget.TextView;
//
//import com.google.gson.Gson;
//
//import java.io.IOException;
//import java.security.InvalidKeyException;
//import java.security.MessageDigest;
//import java.security.NoSuchAlgorithmException;
//import java.security.SecureRandom;
//import java.security.spec.InvalidKeySpecException;
//import java.util.UUID;
//
//import javax.crypto.Mac;
//import javax.crypto.SecretKey;
//import javax.crypto.SecretKeyFactory;
//import javax.crypto.spec.PBEKeySpec;
//
//import okhttp3.MediaType;
//import okhttp3.OkHttpClient;
//import okhttp3.Request;
//import okhttp3.RequestBody;
//import okhttp3.Response;
//
//public class AsymmAuthProcess implements Runnable{
//
//    private final TextView ip;
//    private final TextView msg;
//    private final Gson gson;
//    private final OkHttpClient client;
//    private AddMessage addMessage;
////    private Switch useIccid;
//    private int code;
//
//    public AsymmAuthProcess(View authView){
//        ip = authView.findViewById(R.id.insert_ip);
//        msg = authView.findViewById(R.id.insert_message);
////        secureRandom = new SecureRandom();
//        gson = new Gson();
//        client = new OkHttpClient();
////        useIccid = authView.findViewById(R.id.use_iccid);
//    }
//
//    public void run() {
//
//        if(msg.getText().toString().equals("") || ip.getText().toString().equals("")){
//            code = 0;
//            return;
//        }
//        // change this to the right endpoint of asymm
//        String url = "http://" + ip.getText().toString() + ":3001/api/add_elements";
//        MediaType JSON = MediaType.get("application/json; charset=utf-8");
//        int nIteration = 1000;
//        int keyLength = 256;
//
//        byte[] digest;
//        try {
//            secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA384");
//            keyBytes = secretKeyFactory.generateSecret(pbKeySpec);
//            Mac hmac = Mac.getInstance("HmacSHA384");
//            MessageDigest md = MessageDigest.getInstance("SHA-384");
//            digest = md.digest(hashable.getBytes());
//            hmac.init(keyBytes);
//            kDigest = ConversionUtil.bytesToHex(hmac.doFinal(digest));
//        } catch (NoSuchAlgorithmException | InvalidKeySpecException |
//                 InvalidKeyException e) {
//            throw new RuntimeException(e);
//        }
//        addMessage = new AddMessage(kDigest, ConversionUtil.bytesToHex(digest), msg.getText().toString(), salt, id, useIccid.isChecked() ? iccid : null);
//        String requestBody = gson.toJson(addMessage);
//        try {
//            RequestBody body = RequestBody.create(requestBody, JSON);
//            Request request = new Request.Builder()
//                    .url(url)
//                    .post(body)
//                    .build();
//            Response response = client.newCall(request).execute();
//            code = response.code();
//        } catch (IOException e) {
//            code = 0;
//        }
//    }
//
//    public int getCode(){
//        return code;
//    }
//
//
//}
