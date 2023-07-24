package com.example.uniqueiddemo;

import static com.example.uniqueiddemo.MainActivity.iccid;
import static com.example.uniqueiddemo.MainActivity.keyPair;
import static com.example.uniqueiddemo.MainActivity.modulus;
import static com.example.uniqueiddemo.MainActivity.privExponent;
import static com.example.uniqueiddemo.MainActivity.pubExponent;

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

        byte[] drmidvalue = wvDrm.getPropertyByteArray(MediaDrm.PROPERTY_DEVICE_UNIQUE_ID);

        // p = drm id
        // q = iccid if low android version, otherwise hash of drm id
        int reps = 2;
//        BigInteger tmp_p = new BigInteger(wvDrm.getPropertyByteArray(MediaDrm.PROPERTY_DEVICE_UNIQUE_ID));
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

        System.out.println("bighash(hex len " + bighash_p.length() +  ") is " + bighash_p);
        p = new BigInteger(ConversionUtil.hexStringToByteArray(bighash_p));
//        tmp_q = new BigInteger(ConversionUtil.hexStringToByteArray(bighash_q));


        if (Build.VERSION.SDK_INT <= 30) {
            System.out.println("Setting tmp_q to iccid value hashed");
            try {
                for(int i = 0; i < reps; i++) {
                    bighash_q += ConversionUtil.bytesToHex(sha512(iccid.getBytes()));
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
//            tmp_q = new BigInteger(iccid);
        } else {
            System.out.println("Cannot use iccid, using number only hashed drmid");
            try {
                String drmhash_numberonly = ConversionUtil.bytesToHex(drmidvalue).replaceAll("([a-z])", "");
                for(int i = 0; i < reps; i++) {
                    bighash_q += ConversionUtil.bytesToHex(sha512(ConversionUtil.hexStringToByteArray(drmhash_numberonly)));
                }
//                tmp_q = new BigInteger(ConversionUtil.hexStringToByteArray(drmid_hash));
//                System.out.println("DRM_ID: " + byteArrayToHexString(wvDrm.getPropertyByteArray(MediaDrm.PROPERTY_DEVICE_UNIQUE_ID)));
//                System.out.println("DRM_ID Hash: " + drmid_hash);
//                System.out.println("DRM_ID Hash w/o letters: " + drmid_hash.replaceAll("([a-z])", ""));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }

        System.out.println("bighash q: " + bighash_q);
        q = new BigInteger(ConversionUtil.hexStringToByteArray(bighash_q));

        q = q.nextProbablePrime();
        p = p.nextProbablePrime();
        System.out.println("p prime is: " + p);
        System.out.println("q prime is: " + q);

        BigInteger N = p.multiply(q);
        System.out.println("N is " + N.bitLength() + " bit long");
        System.out.println("N is " + N);
        System.out.println("N is " + ConversionUtil.bytesToHex(N.toByteArray()));
        BigInteger phi = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE));
        BigInteger publicExponent = new BigInteger("65537");
        System.out.println("Public exponent of value " + publicExponent + " is " + (
                areCoprime(phi, publicExponent) ? "" : "NOT "
                ) + "coprime of " + phi);
        BigInteger privateExponent = publicExponent.modInverse(phi);

        try {
            RSAPublicKeySpec spec = new RSAPublicKeySpec(N, publicExponent);
            RSAPrivateKeySpec privateSpec = new RSAPrivateKeySpec(N, privateExponent);

            KeyFactory factory = KeyFactory.getInstance("RSA");

            PublicKey pub = factory.generatePublic(spec);
            PrivateKey priv = factory.generatePrivate(privateSpec);

            byte[] encodedPub = pub.getEncoded();
            System.out.println("Public Key: " + keyToString(encodedPub));

            // This does not work, but public key is the same everytime, so this should be too
//            byte[] encodedPriv = priv.getEncoded();
//            System.out.println("Private Key: " + keyToString(encodedPriv));
            keyPair = new KeyPair(pub, priv);
            modulus = ConversionUtil.bytesToHex(N.toByteArray());
            pubExponent = ConversionUtil.bytesToHex(publicExponent.toByteArray());
            privExponent = ConversionUtil.bytesToHex(privateExponent.toByteArray());
        } catch (Exception e) {
            System.out.println(e.toString());
            throw new RuntimeException(e);
        }

        // test signature
//        String plaintext = "Firmina";
//        try {
//            // sign
//            String algo = "SHA384withRSA";
//            Signature signature = Signature.getInstance(algo);
//            signature.initSign((PrivateKey) keyPair.getPrivate());
//            signature.update(plaintext.getBytes("UTF-8"));
//            byte[] rsa_text = signature.sign();
//            System.out.println("Siganture is: " + byteArrayToHexString(rsa_text));
//
//            // verify
//            Signature verify = Signature.getInstance(algo);
//            verify.initVerify((PublicKey) keyPair.getPublic());
//            verify.update(plaintext.getBytes("UTF-8"));
//            boolean valid = verify.verify(rsa_text);
//            System.out.println("Siganture is " + (valid ? "" : "NOT ") + "valid.");
//        } catch (Exception e) {
//            System.out.println(e.toString());
//            throw new RuntimeException(e);
//        }
    }

    public static boolean areCoprime(BigInteger m, BigInteger t) {
        return m.gcd(t).equals(BigInteger.ONE);
    }

    public static String keyToString(byte[] key) {
        return Base64.getEncoder().encodeToString(key);
    }

    public static byte[] sha512(byte[] input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        md.update(input);
        return md.digest();
    }
}
