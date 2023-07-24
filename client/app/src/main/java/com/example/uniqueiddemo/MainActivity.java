package com.example.uniqueiddemo;

import static com.example.uniqueiddemo.ConversionUtil.PERMISSIONS_REQUEST_READ_PHONE_STATE;
import static com.google.android.material.R.color.design_default_color_primary_variant;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.widget.Toast;

import com.example.uniqueiddemo.databinding.ActivityMainBinding;

import java.io.StringWriter;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    static String iccid;
    static KeyPair keyPair;

    static boolean needToHandshake = false;
    static String modulus, pubExponent, privExponent;
    static String sharedPrefName = "com.example.uniqueiddemo.asymm";

    static int serialNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Verifica se il permesso READ_PHONE_STATE è stato concesso
        int permissionCheck = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_STATE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT <= 30) {
            // Se il permesso non è stato concesso, richiedilo all'utente
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_PHONE_STATE},
                    PERMISSIONS_REQUEST_READ_PHONE_STATE);
        } else if (Build.VERSION.SDK_INT <= 30) {
            TelecomManager tm2 = (TelecomManager) getSystemService(Context.TELECOM_SERVICE);
            Iterator<PhoneAccountHandle> phoneAccounts = tm2.getCallCapablePhoneAccounts().listIterator();
            PhoneAccountHandle phoneAccountHandle = phoneAccounts.next();
            iccid = phoneAccountHandle.getId().substring(0, 19);
        } else if (Build.VERSION.SDK_INT > 30){
            iccid = null;
        }

        Context context = this;
        SharedPreferences sharedPref = context.getSharedPreferences(sharedPrefName, Context.MODE_PRIVATE);
        serialNumber = sharedPref.getInt("serialNumber", -1);
        if(serialNumber == -1) {
            needToHandshake = true;
            // We do not have a serialNumber, so start handshake phase
            AsymmHandshakeHandler.keyGen();
            System.out.println("pubk: "+ keyPair.getPublic().toString());
            // Now I have the static params populated, let's save in the shared prefs
            SharedPreferences.Editor sharedEditor = sharedPref.edit();
            sharedEditor.putString("modulus", modulus);
            sharedEditor.putString("pubexponent", pubExponent);
            sharedEditor.putString("privexponent", privExponent);
            sharedEditor.apply();
        } else {
            // I have data in sharedprefs, load them
            modulus = sharedPref.getString("modulus", "nope");
            pubExponent = sharedPref.getString("pubexponent", "nope");
            privExponent = sharedPref.getString("privexponent", "nope");
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
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSIONS_REQUEST_READ_PHONE_STATE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(getApplicationContext(), "Permission granted", Toast.LENGTH_SHORT).show();
                    TelecomManager tm2 = (TelecomManager) getSystemService(Context.TELECOM_SERVICE);
                    @SuppressLint("MissingPermission")
                    Iterator<PhoneAccountHandle> phoneAccounts = tm2.getCallCapablePhoneAccounts().listIterator();
                    PhoneAccountHandle phoneAccountHandle = phoneAccounts.next();
                    iccid = phoneAccountHandle.getId().substring(0,19);

                } else {
                    Toast.makeText(getApplicationContext(), "The ICCID will not be used to generate the key", Toast.LENGTH_SHORT).show();
                    iccid = null;
                }
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private String publicKeyToPEM(PublicKey pubKey) {
        String pk = Base64.getEncoder().encodeToString(pubKey.getEncoded());
        String begin = "-----BEGIN PUBLIC KEY----- ";
        String end = " -----END PUBLIC KEY-----";
//        return (begin + pk.replaceAll("(.{64})", "$1\n") + end);
        return (begin + pk + end);
    }

    private void replaceFragment(Fragment fragment){

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }

}