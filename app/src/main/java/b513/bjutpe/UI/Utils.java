package b513.bjutpe.UI;

import android.content.SharedPreferences;
import android.util.Base64;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.SecureRandom;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

//这里放通用的静态方法
public class Utils {

    static public String SPREFNAME_MAIN = "main";

    static public String CRYPTO_KEY_MAIN = "zxcvbnm";
    //(加密密钥就xjb写了，反正也没卵用)

    static public String SPLIT_LOGININFOS = " ";
    //分隔符。使用用户名密码里都不可能有的字符

    static private byte[] endecrypt(String skey, byte[] data, int mode) throws Exception {
        KeyGenerator keygen = KeyGenerator.getInstance("AES");
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG", "Crypto");
        sr.setSeed(skey.getBytes());
        keygen.init(128, sr);
        SecretKey kkey = keygen.generateKey();
        byte[] bkey = kkey.getEncoded();
        SecretKeySpec fkey = new SecretKeySpec(bkey, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(mode, fkey);
        return cipher.doFinal(data);
    }

    static public String encrypt(String skey, String data) {
        try {
            byte[] result = endecrypt(skey, data.getBytes(), Cipher.ENCRYPT_MODE);
            return Base64.encodeToString(result, 0);
        } catch (Exception e) {
            return null;
        }
    }

    static public String decrypt(String skey, String data) {
        try {
            byte[] datta = Base64.decode(data, 0);
            byte[] result = endecrypt(skey, datta, Cipher.DECRYPT_MODE);
            return new String(result);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String s = sw.toString();
            pw.close();
            return s;
        }
    }

    static public boolean getLoginInfos(SharedPreferences sp, List<String> unames, List<String> passwds) {
        String sunames = sp.getString("unames", null);
        String spasswds = sp.getString("passwds", null);
        if (sunames == null || spasswds == null) {
            return false;
        }
        sunames = decrypt(CRYPTO_KEY_MAIN, sunames);
        spasswds = decrypt(CRYPTO_KEY_MAIN, spasswds);
        if (sunames == null || spasswds == null) {
            return false;
        }
        String[] aunames = sunames.split(SPLIT_LOGININFOS);
        String[] apasswds = spasswds.split(SPLIT_LOGININFOS);
        for (String s : aunames) {
            unames.add(s);
        }
        for (String s : apasswds) {
            passwds.add(s);
        }
        return true;
    }

    static public boolean saveLoginInfos(SharedPreferences sp, List<String> unames, List<String> passwds) {
        StringBuilder sbu = new StringBuilder();
        for (String s : unames) {
            sbu.append(s).append(SPLIT_LOGININFOS);
        }
        String su = sbu.deleteCharAt(sbu.length() - 1).toString();
        StringBuilder sbp = new StringBuilder();
        for (String s : passwds) {
            sbp.append(s).append(SPLIT_LOGININFOS);
        }
        String sq = sbp.deleteCharAt(sbp.length() - 1).toString();
        su = encrypt(CRYPTO_KEY_MAIN, su);
        sq = encrypt(CRYPTO_KEY_MAIN, sq);
        sp.edit().putString("unames", su).putString("passwds", sq).apply();
        return true;
    }
}
