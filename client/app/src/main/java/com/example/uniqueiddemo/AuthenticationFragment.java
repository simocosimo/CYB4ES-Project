package com.example.uniqueiddemo;

import static com.example.uniqueiddemo.MainActivity.iccid;
import static com.example.uniqueiddemo.MainActivity.needToHandshake;
import static com.example.uniqueiddemo.MainActivity.serialNumber;
import static com.example.uniqueiddemo.MainActivity.sharedPrefName;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.UnsupportedSchemeException;
import android.os.Build;
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
import android.widget.Switch;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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

    private RadioGroup enc;
    private RadioButton type;
    private Thread netThread;
    private Switch useIccid;

    public AuthenticationFragment() {
        // Required empty public constructor
    }
    public static AuthenticationFragment newInstance(String param1, String param2) {
        AuthenticationFragment fragment = new AuthenticationFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View authView = inflater.inflate(R.layout.fragment_authentication, container, false);
        Button auth = authView.findViewById(R.id.auth_button);
        enc = authView.findViewById(R.id.radio_group);
        type = authView.findViewById(R.id.radio_symm);
        useIccid = authView.findViewById(R.id.use_iccid);
        if (Build.VERSION.SDK_INT > 30){
            useIccid.setVisibility(View.INVISIBLE);
        } else {
            useIccid.setChecked(false);
        }
        if(iccid == null){
            useIccid.setEnabled(false);
        }

        enc.setOnCheckedChangeListener((radioGroup, i) -> {
            if (i == R.id.radio_symm) {
                // Symmetric encryption radio button is chosen
                auth.setEnabled(true);
                useIccid.setEnabled(true);
                type = authView.findViewById(i);
                Toast.makeText(authView.getContext(), type.getText() + " chosen", Toast.LENGTH_SHORT).show();
            } else if (i == R.id.radio_asymm) {
                // Asymmetric encryption radio button is chosen
                type = authView.findViewById(i);
                useIccid.setEnabled(false);
                if(needToHandshake) {
                    // No data saved in sharedPreferences, so handshake needs to be executed
                    auth.setEnabled(false);
                    Toast.makeText(authView.getContext(), type.getText() + " chosen, handshake in progress", Toast.LENGTH_SHORT).show();
                    AsymmHandshakeProcess ap = new AsymmHandshakeProcess(authView);
                    try {
                        // Thread contacting server for handshake
                        netThread = new Thread(ap);
                        netThread.start();
                        netThread.join();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    // Handling the result
                    int code = ap.getCode();
                    if(code == 0) {
                        Toast.makeText(getContext(),"Please fill IP field for handshake phase",Toast.LENGTH_SHORT).show();
                        RadioButton sy = authView.findViewById(R.id.radio_symm);
                        sy.setChecked(true);
                    } else if(code == 201) {
                        Toast.makeText(getContext(),"Handshake completed",Toast.LENGTH_SHORT).show();
                        JsonObject resjson = JsonParser.parseString(ap.getBody()).getAsJsonObject();
                        int sn = resjson.get("serialNumber").getAsInt();
                        System.out.println("Serial number is: " + sn);
                        // save the serialnumber into shared preferences and static value
                        serialNumber = sn;
                        SharedPreferences sharedpref = this.getActivity().getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE);
                        SharedPreferences.Editor sharededitor = sharedpref.edit();
                        sharededitor.putInt("serialNumber", sn);
                        sharededitor.apply();
                        auth.setEnabled(true);
                        needToHandshake = false;
                    }
                } else {
                    Toast.makeText(authView.getContext(), type.getText() + " chosen", Toast.LENGTH_SHORT).show();
                }
            }
        });
        auth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Send message according to the chosen encryption
                if(type.getId() == R.id.radio_symm) {
                    SymmAuthProcess symmAuthProcess = new SymmAuthProcess(authView);
                    try {
                        netThread = new Thread(symmAuthProcess);
                        netThread.start();
                        netThread.join();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    int code = symmAuthProcess.getCode();
                    if(code == 0){
                        Toast.makeText(getContext(),"Please fill both input fields",Toast.LENGTH_SHORT).show();
                    }
                    if (code == 200){
                        Toast.makeText(getContext(),"Message sent correctly",Toast.LENGTH_SHORT).show();
                    } else if (code == 501){
                        Toast.makeText(getContext(),"HMAC not well formed",Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(),"Somethig went wrong: error code " + code, Toast.LENGTH_SHORT).show();
                    }
                } else if (type.getId() == R.id.radio_asymm) {
                    AsymmMessageProcess asymmMessageProcess = new AsymmMessageProcess(authView);
                    try {
                        netThread = new Thread(asymmMessageProcess);
                        netThread.start();
                        netThread.join();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                    int code = asymmMessageProcess.getCode();
                    if(code == 0){
                        Toast.makeText(getContext(),"Please fill both input fields",Toast.LENGTH_SHORT).show();
                    } else if (code == 201){
                        Toast.makeText(getContext(),"Message sent correctly",Toast.LENGTH_SHORT).show();
                    } else if (code == 503){
                        Toast.makeText(getContext(),"Server signal wrong signature",Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(),"Somethig went wrong: error code " + code, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        return authView;
    }
}