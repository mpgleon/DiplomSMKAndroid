package com.example.diplomsmkandroid.util;

import at.favre.lib.crypto.bcrypt.BCrypt;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public final class SecurityUtil {

    private SecurityUtil() {}

    public static String hashPassword(String password) {
        return BCrypt.withDefaults().hashToString(12, password.toCharArray());
    }

    public static boolean verifyPassword(String password, String hashed) {
        if (hashed == null || hashed.isEmpty()) return false;
        if (hashed.startsWith("$2")) {
            BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), hashed);
            return result.verified;
        }
        // Legacy: SHA-256
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString().equals(hashed);
        } catch (Exception e) {
            return false;
        }
    }
}
