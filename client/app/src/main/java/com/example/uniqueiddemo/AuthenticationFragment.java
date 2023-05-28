package com.example.uniqueiddemo;

import android.media.UnsupportedSchemeException;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.media.MediaDrm;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AuthenticationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AuthenticationFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final UUID WIDEVINE_UUID = new UUID(-0x121074568629b532L, -0x5c37d8232ae2de13L);
    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private RadioGroup enc;
    private RadioButton type;
    private EditText msg;
    private EditText ip;
    private OkHttpClient client;
    private SecureRandom secureRandom;
    private MediaDrm wvDrm;
    private Gson gson;
    private AddMessage addMessage;
    private Thread netThread;

    public AuthenticationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AuthenticationFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AuthenticationFragment newInstance(String param1, String param2) {
        AuthenticationFragment fragment = new AuthenticationFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View authView = inflater.inflate(R.layout.fragment_authentication, container, false);
        Button auth = authView.findViewById(R.id.auth_button);
        enc = authView.findViewById(R.id.radio_group);
        msg = authView.findViewById(R.id.insert_message);
        ip = authView.findViewById(R.id.insert_ip);
        secureRandom = new SecureRandom();
        gson = new Gson();
        client = new OkHttpClient();
        type = authView.findViewById(R.id.radio_symm);
        try {
            wvDrm = new MediaDrm(WIDEVINE_UUID);
        } catch (UnsupportedSchemeException e) {
            throw new RuntimeException(e);
        }

        enc.setOnCheckedChangeListener((radioGroup, i) -> {

            if (i == R.id.radio_symm){
                type = authView.findViewById(i);
                Toast.makeText(authView.getContext(), type.getText() + " chosen", Toast.LENGTH_SHORT).show();
            }else if (i == R.id.radio_asymm){
                type = authView.findViewById(i);
                Toast.makeText(authView.getContext(), type.getText() + " chosen", Toast.LENGTH_SHORT).show();
            }


        });
        auth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                netThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String url = "http://" + ip.getText().toString() + ":3001/api/add_elements";
                        MediaType JSON = MediaType.get("application/json; charset=utf-8");
                        byte saltBytes[] = new byte[8];
                        secureRandom.nextBytes(saltBytes);
                        String salt = bytesToHex(saltBytes);
                        int code = 0;
                        if (type.getId() == R.id.radio_symm) {
                            String hashable = msg.getText().toString() + salt;
                            int nIteration = 1000;
                            int keyLength = 256;
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
                            } catch (NoSuchAlgorithmException e) {
                                throw new RuntimeException(e);
                            } catch (InvalidKeySpecException e) {
                                throw new RuntimeException(e);
                            } catch (InvalidKeyException e) {
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

                            }

                        }
                    }
                });
                netThread.start();
            }
        });
        return authView;
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

}