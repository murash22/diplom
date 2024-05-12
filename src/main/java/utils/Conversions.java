package utils;


import jakarta.xml.bind.DatatypeConverter;

import java.nio.charset.Charset;


public final class Conversions {
    public static final Charset encoding = Charset.forName("ISO-8859-5");

    public static byte[] hex(String string) {
        string = string.replace(" ", "");
        if (string.length() % 2 == 1)
            string = string.substring(0, string.length() - 1) + "0" + string.charAt(string.length() - 1);
        return DatatypeConverter.parseHexBinary(string);
    }

    public static String hex(byte[] buffer) {
        return DatatypeConverter.printHexBinary(buffer).replaceAll("(.{2})", "$1 ").strip();
    }


    public static byte[] raw(String string) {
        return string.getBytes(encoding);
    }

    public static String rawS(String string) {
        return string;
    }

    public static String raw(byte[] buffer) {
        return new String(buffer, encoding);
    }


    public static int word(byte[] bytes) {
        return (bytes[0] << 24) | ((bytes[1] & 0xff) << 16) | ((bytes[2] & 0xff) << 8) | (bytes[3] & 0xff);
    }


    public static byte[] word(int integer) {
        return new byte[]{(byte) (integer >> 24), (byte) ((integer >> 16) & 0xff), (byte) ((integer >> 8) & 0xff), (byte) (integer & 0xff)};
    }


}
