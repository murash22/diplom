package algorithm;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.util.Arrays;
import java.util.Objects;

public class Algorithm {
    private SecretKey key;
    private Cipher ecnCipher;
    private Cipher decCipher;
//    private IvParameterSpec IV = new IvParameterSpec(new byte[] {0x77, (byte) 0xAA, (byte) 0x88, (byte) 0x99, 0x01, 0x43, (byte) 0x85, 0x67});
    public final int BLOCK_SIZE = 8;

    public Algorithm(String mode) {
        try {
            KeyGenerator generator = KeyGenerator.getInstance("DES");
            key = generator.generateKey();
            String cipherInfo = "DES/" + mode + "/NoPadding";
            ecnCipher = Cipher.getInstance(cipherInfo);  // без паддинга, чтобы дополнять по стандарту 80 00 00
            ecnCipher.init(Cipher.ENCRYPT_MODE, key);
            decCipher = Cipher.getInstance(cipherInfo);
            if (Objects.equals(mode, "CBC")) {
                decCipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(ecnCipher.getIV()));
            } else {
                decCipher.init(Cipher.DECRYPT_MODE, key);
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public byte[] encrypt(byte[] plaintext) {
        byte[] padded = pad(plaintext);
        try {
            return ecnCipher.doFinal(padded);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public byte[] decrypt(byte[] encrypted, boolean removePadding) {
        try {
            return removePadding ? unpad(decCipher.doFinal(encrypted)) : decCipher.doFinal(encrypted);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    private byte[] pad(byte[] data) {
        int padSize = BLOCK_SIZE - data.length % BLOCK_SIZE;
        byte[] result = new byte[data.length + padSize];
        System.arraycopy(data, 0, result, 0, data.length);
        result[data.length] = (byte) 0x80;
        Arrays.fill(result, data.length + 1, result.length, (byte) 0x0);
        return result;
    }

    private byte[] unpad(byte[] data) {
        int padIndex = data.length - 1;

        while (data[padIndex] == (byte) 0x0) {
            padIndex -= 1;
        }
        return Arrays.copyOfRange(data, 0, padIndex);
    }
}
