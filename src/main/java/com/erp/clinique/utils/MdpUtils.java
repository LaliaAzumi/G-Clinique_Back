package com.erp.clinique.utils;

import java.security.SecureRandom;

public class MdpUtils {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$%&*!?";
    private static final int DEFAULT_LENGTH = 10;
    private static final SecureRandom random = new SecureRandom();

    public static String generateRandomMdp() {
        return generateRandomMdp(DEFAULT_LENGTH);
    }

    public static String generateRandomMdp(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(index));
        }
        return sb.toString();
    }
}