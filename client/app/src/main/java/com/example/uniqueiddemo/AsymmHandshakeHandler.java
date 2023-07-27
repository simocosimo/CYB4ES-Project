package com.example.uniqueiddemo;

import static com.example.uniqueiddemo.MainActivity.iccid;
import static com.example.uniqueiddemo.MainActivity.keyPair;
import static com.example.uniqueiddemo.MainActivity.modulus;
import static com.example.uniqueiddemo.MainActivity.needToHandshake;
import static com.example.uniqueiddemo.MainActivity.privExponent;
import static com.example.uniqueiddemo.MainActivity.pubExponent;

import android.content.SharedPreferences;
import android.icu.text.SymbolTable;
import android.media.MediaDrm;
import android.media.UnsupportedSchemeException;
import android.os.Build;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.UUID;

public class AsymmHandshakeHandler {

    private static MediaDrm wvDrm;
    private static final UUID WIDEVINE_UUID = new UUID(-0x121074568629b532L, -0x5c37d8232ae2de13L);

    // This needs to return a value that comprehends an exception
    public static void keyGen() {

        try {
            wvDrm = new MediaDrm(WIDEVINE_UUID);
        } catch (UnsupportedSchemeException e) {
            throw new RuntimeException(e);
        }

        // save the DRM ID value
        byte[] drmidvalue = wvDrm.getPropertyByteArray(MediaDrm.PROPERTY_DEVICE_UNIQUE_ID);

        int reps = 2;
        BigInteger p;
        BigInteger q;
        String bighash_p = "";
        String bighash_q = "";

        try {
            for(int i = 0; i < reps; i++) {
                bighash_p += ConversionUtil.bytesToHex(sha512(drmidvalue));
            }
        } catch (Exception e) {
            System.out.println(e);
        }

        p = new BigInteger(ConversionUtil.hexStringToByteArray(bighash_p));

        if (iccid != null) {
            System.out.println("Setting tmp_q to iccid value hashed");
            try {
                for(int i = 0; i < reps; i++) {
                    bighash_q += ConversionUtil.bytesToHex(sha512(iccid.getBytes()));
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            System.out.println("Cannot use iccid, using number only hashed drmid");
            try {
                String drmhash_numberonly = ConversionUtil.bytesToHex(drmidvalue).replaceAll("([a-z])", "");
                for(int i = 0; i < reps; i++) {
                    bighash_q += ConversionUtil.bytesToHex(sha512(drmhash_numberonly.getBytes()));
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        q = new BigInteger(ConversionUtil.hexStringToByteArray(bighash_q));

        // Generate the actual p and q, plus N, e and phi, to calculate D
        q = q.abs();
        p = p.abs();
        q = q.nextProbablePrime();
        p = p.nextProbablePrime();
        System.out.println("p prime is: " + p);
        System.out.println("q prime is: " + q);

        BigInteger N = p.multiply(q);
        System.out.println("N is " + N);
        BigInteger phi = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE));
        BigInteger publicExponent = new BigInteger("65537");
        BigInteger privateExponent = publicExponent.modInverse(phi);

        try {
            // save the keys to the static variable and also to the SharedPreferences
            saveKeyPair(N, publicExponent, privateExponent);
            modulus = ConversionUtil.bytesToHex(N.toByteArray());
            pubExponent = ConversionUtil.bytesToHex(publicExponent.toByteArray());
            privExponent = ConversionUtil.bytesToHex(privateExponent.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Sign the message using the SHA384withRSA algorithm, returning the hex string of the
    // signature itself
    public static String signMessage(String plaintext) {
        try {
            String algo = "SHA384withRSA";
            Signature signature = Signature.getInstance(algo);
            signature.initSign((PrivateKey) keyPair.getPrivate());
            signature.update(plaintext.getBytes("UTF-8"));
            byte[] rsa_text = signature.sign();
            return ConversionUtil.bytesToHex(rsa_text);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // This function creates the key pair and save it to the shared preferences
    // it sets the needs to handshake to true
    public static void keyGenAndStore(SharedPreferences sharedPref, String iccid) {
        needToHandshake = true;
        keyGen();
        System.out.println("pubk: "+ keyPair.getPublic().toString());
        // Now I have the static params populated, let's save in the shared prefs
        SharedPreferences.Editor sharedEditor = sharedPref.edit();
        sharedEditor.putString("modulus", modulus);
        sharedEditor.putString("pubexponent", pubExponent);
        sharedEditor.putString("privexponent", privExponent);
        if(iccid != null) sharedEditor.putString("iccid", iccid);
        sharedEditor.apply();
    }

    // Generate the key pair starting from the RSA values (n, e, d) and save them in the static
    // keyPair variable
    public static void saveKeyPair(BigInteger n, BigInteger pubexp, BigInteger privexp) throws NoSuchAlgorithmException, InvalidKeySpecException {
        RSAPublicKeySpec spec = new RSAPublicKeySpec(n, pubexp);
        RSAPrivateKeySpec privateSpec = new RSAPrivateKeySpec(n, privexp);

        KeyFactory factory = KeyFactory.getInstance("RSA");
        PublicKey pub = factory.generatePublic(spec);
        PrivateKey priv = factory.generatePrivate(privateSpec);
        keyPair = new KeyPair(pub, priv);
    }

    public static byte[] sha512(byte[] input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        md.update(input);
        return md.digest();
    }

    public static byte[] sha384(byte[] input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-384");
        md.update(input);
        return md.digest();
    }
}
