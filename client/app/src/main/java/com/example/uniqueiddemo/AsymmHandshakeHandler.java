package com.example.uniqueiddemo;

import static com.example.uniqueiddemo.MainActivity.iccid;
import static com.example.uniqueiddemo.MainActivity.keyPair;

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

        // p = drm id
        // q = iccid if low android version, otherwise hash of drm id
        int bitTargetLength = 1024;
        BigInteger tmp_p = new BigInteger(wvDrm.getPropertyByteArray(MediaDrm.PROPERTY_DEVICE_UNIQUE_ID));
        BigInteger tmp_q;

        if (Build.VERSION.SDK_INT <= 30) {
            System.out.println("Setting tmp_q to iccid value: " + iccid);
            tmp_q = new BigInteger(iccid);
        } else {
            try {
                // cannot use hash for the q prime, since it contains letters. So we remove them
                String drmid_hash = byteArrayToHexString(sha256(wvDrm.getPropertyByteArray(MediaDrm.PROPERTY_DEVICE_UNIQUE_ID)));
                tmp_q = new BigInteger(drmid_hash.replaceAll("([a-z])", ""));
                System.out.println("DRM_ID: " + byteArrayToHexString(wvDrm.getPropertyByteArray(MediaDrm.PROPERTY_DEVICE_UNIQUE_ID)));
                System.out.println("DRM_ID Hash: " + drmid_hash);
                System.out.println("DRM_ID Hash w/o letters: " + drmid_hash.replaceAll("([a-z])", ""));
            } catch (Exception e) {
                System.out.println("Setting tmp_q to 1 because of hash error");
                tmp_q = new BigInteger("1");
            }

        }

        System.out.println("tmp_q before shift: " + tmp_q);
        System.out.println("tmp_p before shift: " + tmp_p);
        tmp_p = tmp_p.shiftLeft(bitTargetLength - tmp_p.bitLength());
        tmp_q = tmp_q.shiftLeft(bitTargetLength - tmp_q.bitLength());
        System.out.println("tmp_q after shift: " + tmp_q);
        System.out.println("tmp_p after shift: " + tmp_p);

        BigInteger q = tmp_q.nextProbablePrime();
        BigInteger p = tmp_p.nextProbablePrime();
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
        } catch (Exception e) {
            System.out.println(e.toString());
            throw new RuntimeException(e);
        }

        // test signature
        String plaintext = "Firmina";
        try {
            // sign
            String algo = "SHA384withRSA";
            Signature signature = Signature.getInstance(algo);
            signature.initSign((PrivateKey) keyPair.getPrivate());
            signature.update(plaintext.getBytes("UTF-8"));
            byte[] rsa_text = signature.sign();
            System.out.println("Siganture is: " + byteArrayToHexString(rsa_text));

            // verify
            Signature verify = Signature.getInstance(algo);
            verify.initVerify((PublicKey) keyPair.getPublic());
            verify.update(plaintext.getBytes("UTF-8"));
            boolean valid = verify.verify(rsa_text);
            System.out.println("Siganture is " + (valid ? "" : "NOT ") + "valid.");
        } catch (Exception e) {
            System.out.println(e.toString());
            throw new RuntimeException(e);
        }
    }

    public static boolean areCoprime(BigInteger m, BigInteger t) {
        return m.gcd(t).equals(BigInteger.ONE);
    }

    public static String keyToString(byte[] key) {
        return Base64.getEncoder().encodeToString(key);
    }

    public static String byteArrayToHexString(byte[] bytes) {
        StringBuffer buffer = new StringBuffer();
        for(int i=0; i<bytes.length; i++) {
            if(((int)bytes[i] & 0xff) < 0x10) buffer.append("0");
            buffer.append(Long.toString((int) bytes[i] & 0xff, 16));
        }
        return buffer.toString();
    }

    public static byte[] sha256(byte[] input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(input);
        return md.digest();
    }
}
