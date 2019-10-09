package com.iyzico.challenge.integrator.util;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Objects;

public class IntegrationStringUtils {
    private final static SecureRandom tokenRandom = new SecureRandom();
    private final static char[] HAX_CHARS = "0123456789abcdef".toCharArray();

    private IntegrationStringUtils() {
    }

    public static String generate(int length) {
        byte[] random = new byte[length];

        tokenRandom.nextBytes(random);
        ByteBuffer keyBuf = ByteBuffer.allocate(length + 8);
        keyBuf.put(random);
        keyBuf.putLong(System.currentTimeMillis());

        return encode(keyBuf.array());
    }

    private static String encode(final byte[] bytes) {
        Objects.requireNonNull(bytes, "bytes cannot be null");

        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int val = bytes[j] & 0xFF;

            hexChars[j * 2] = HAX_CHARS[val >>> 4];
            hexChars[j * 2 + 1] = HAX_CHARS[val & 0x0F];
        }

        return new String(hexChars);
    }
}
