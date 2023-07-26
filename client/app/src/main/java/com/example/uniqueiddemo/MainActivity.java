package com.example.uniqueiddemo;

import static com.example.uniqueiddemo.ConversionUtil.PERMISSIONS_REQUEST_READ_PHONE_STATE;
import static com.google.android.material.R.color.design_default_color_primary_variant;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.widget.Switch;
import android.widget.Toast;

import com.example.uniqueiddemo.databinding.ActivityMainBinding;

import java.io.StringWriter;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    static String iccid = null;
    static KeyPair keyPair;
    static boolean needToHandshake = false;
    static String modulus, pubExponent, privExponent;
    static String sharedPrefName = "com.example.uniqueiddemo.asymm";
    static int serialNumber;
    static SharedPreferences sharedPref;
    private Switch useIccid;
    static public int permissionCheck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Context context = this;
        sharedPref = context.getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE);
        serialNumber = sharedPref.getInt("serialNumber", -1);
        String mod = sharedPref.getString("modulus", "null");
        iccid = sharedPref.getString("iccid", null);
        System.out.println("SerialNumber: " + serialNumber);

        if(Build.VERSION.SDK_INT <= 30){
            permissionCheck = PermissionChecker.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_STATE);
        }
        if(mod == "null" && serialNumber == -1) {
            // If no info is saved in the SharedPreferences, it is the first time the app is being run
            // so aks for permissions
            if (permissionCheck != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT <= 30) {
                // If permissions are not granted, ask for them
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_PHONE_STATE},
                        PERMISSIONS_REQUEST_READ_PHONE_STATE);
            } else if (Build.VERSION.SDK_INT <= 30) {
                // If permissions are granted, get the iccid value
                TelecomManager tm2 = (TelecomManager) getSystemService(Context.TELECOM_SERVICE);
                Iterator<PhoneAccountHandle> phoneAccounts = tm2.getCallCapablePhoneAccounts().listIterator();
                PhoneAccountHandle phoneAccountHandle = phoneAccounts.next();
                iccid = phoneAccountHandle.getId().substring(0, 19);
            } else if (Build.VERSION.SDK_INT > 30){
                // If permissions for the iccid cannot be granted, generate the key without it
                iccid = null;
                if(serialNumber == -1) {
                    needToHandshake = true;
                    AsymmHandshakeHandler.keyGen();
                    System.out.println("pubk: "+ keyPair.getPublic().toString());
                    // Now I have the static params populated, let's save in the shared prefs
                    SharedPreferences.Editor sharedEditor = sharedPref.edit();
                    sharedEditor.putString("modulus", modulus);
                    sharedEditor.putString("pubexponent", pubExponent);
                    sharedEditor.putString("privexponent", privExponent);
                    sharedEditor.apply();
                }
            }
        }else if(permissionCheck == PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT <= 30){
            TelecomManager tm2 = (TelecomManager) getSystemService(Context.TELECOM_SERVICE);
            Iterator<PhoneAccountHandle> phoneAccounts = tm2.getCallCapablePhoneAccounts().listIterator();
            PhoneAccountHandle phoneAccountHandle = phoneAccounts.next();
            iccid = phoneAccountHandle.getId().substring(0, 19);
            SharedPreferences.Editor sharedEditor = sharedPref.edit();
            sharedEditor.putString("iccid", iccid);
            sharedEditor.commit();
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        replaceFragment(new AuthenticationFragment());

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {

            if (item.getItemId() == R.id.authmenu) {
                replaceFragment(new AuthenticationFragment());
                return true;
            } else if (item.getItemId() == R.id.verifmenu) {
                replaceFragment(new VerificationFragment());
                return true;
            }

            return false;
        });

        // If info is saved in the SharedPreferences, restore them and use it for the rest of
        // the execution. This prevents key re-generation each time the app is run.
        if(serialNumber != -1 || mod != "null") {
            modulus = sharedPref.getString("modulus", "nope");
            pubExponent = sharedPref.getString("pubexponent", "nope");
            privExponent = sharedPref.getString("privexponent", "nope");
            BigInteger m = new BigInteger(ConversionUtil.hexStringToByteArray(modulus));
            BigInteger pe = new BigInteger(ConversionUtil.hexStringToByteArray(pubExponent));
            BigInteger se = new BigInteger(ConversionUtil.hexStringToByteArray(privExponent));
            try {
                AsymmHandshakeHandler.saveKeyPair(m, pe, se);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        useIccid = this.findViewById(R.id.use_iccid);
        switch (requestCode) {
            case PERMISSIONS_REQUEST_READ_PHONE_STATE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // case PERMISSION_GRANTED
                    // Retrieve iccid + generate the key using it as a parameter for the q RSA value
                    useIccid.setEnabled(true);
                    Toast.makeText(getApplicationContext(), "Permission granted", Toast.LENGTH_SHORT).show();
                    TelecomManager tm2 = (TelecomManager) getSystemService(Context.TELECOM_SERVICE);
                    @SuppressLint("MissingPermission")
                    Iterator<PhoneAccountHandle> phoneAccounts = tm2.getCallCapablePhoneAccounts().listIterator();
                    PhoneAccountHandle phoneAccountHandle = phoneAccounts.next();
                    iccid = phoneAccountHandle.getId().substring(0,19);
                    needToHandshake = true;
                    AsymmHandshakeHandler.keyGen();
                    System.out.println("pubk: "+ keyPair.getPublic().toString());
                    // Now I have the static params populated, let's save in the shared prefs
                    SharedPreferences.Editor sharedEditor = sharedPref.edit();
                    sharedEditor.putString("modulus", modulus);
                    sharedEditor.putString("pubexponent", pubExponent);
                    sharedEditor.putString("privexponent", privExponent);
                    sharedEditor.putString("iccid", iccid);
                    sharedEditor.apply();
                } else {
                    // case PERMISSION_DENIED
                    // The key is now generated without the iccid for the q RSA value
                    Toast.makeText(getApplicationContext(), "The ICCID will not be used to generate the key", Toast.LENGTH_SHORT).show();
                    iccid = null;
                    needToHandshake = true;
                    useIccid.setEnabled(false);
                    AsymmHandshakeHandler.keyGen();
                    System.out.println("pubk: "+ keyPair.getPublic().toString());
                    // Now I have the static params populated, let's save in the shared prefs
                    SharedPreferences.Editor sharedEditor = sharedPref.edit();
                    sharedEditor.putString("modulus", modulus);
                    sharedEditor.putString("pubexponent", pubExponent);
                    sharedEditor.putString("privexponent", privExponent);
                    sharedEditor.apply();
                }
            }
        }
    }

    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }

}