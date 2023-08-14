public class TestOAEPWithParams {
    private static Provider cp;
    private static PrivateKey privateKey;
    private static PublicKey publicKey;
    private static Random random = new Random();
    private static String MD[] = {
        "MD5", "SHA1", "SHA-256"
    };
    private static int DATA_LENGTH[] = {
        62, 54, 30
    };
    public static void main(String[] args) throws Exception {
        long start = System.currentTimeMillis();
        cp = Security.getProvider("SunJCE");
        System.out.println("Testing provider " + cp.getName() + "...");
        Provider kfp = Security.getProvider("SunRsaSign");
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA", kfp);
        kpg.initialize(768);
        KeyPair kp = kpg.generateKeyPair();
        privateKey = kp.getPrivate();
        publicKey = kp.getPublic();
        for (int i = 0; i < MD.length; i++) {
            testEncryptDecrypt(MD[i], DATA_LENGTH[i]);
        }
        long stop = System.currentTimeMillis();
        System.out.println("Done (" + (stop - start) + " ms).");
    }
    private static void testEncryptDecrypt(String hashAlg, int dataLength)
        throws Exception {
        System.out.println("Testing OAEP with hash " + hashAlg + ", " + dataLength + " bytes");
        Cipher c = Cipher.getInstance("RSA/ECB/OAEPwith" + hashAlg +
                                      "andMGF1Padding", cp);
        byte[] pSrc1 = { (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
                         (byte) 0x02, (byte) 0x02, (byte) 0x02, (byte) 0x02
        };
        byte[] pSrc2 = { (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01,
                         (byte) 0x02, (byte) 0x02, (byte) 0x03, (byte) 0x04
        };
        OAEPParameterSpec spec1 = new OAEPParameterSpec(hashAlg,
            "MGF1", MGF1ParameterSpec.SHA1, new PSource.PSpecified(pSrc1));
        OAEPParameterSpec spec2 = new OAEPParameterSpec(hashAlg,
            "MGF1", MGF1ParameterSpec.SHA1, new PSource.PSpecified(pSrc2));
        byte[] plainText = new byte[dataLength];
        byte[] cipherText, recovered;
        System.out.println("Testing with user-supplied parameters...");
        c.init(Cipher.ENCRYPT_MODE, publicKey, spec1);
        cipherText = c.doFinal(plainText);
        c.init(Cipher.DECRYPT_MODE, privateKey, spec1);
        recovered = c.doFinal(cipherText);
        if (Arrays.equals(plainText, recovered) == false) {
            throw new Exception("Decrypted data does not match");
        }
        c.init(Cipher.DECRYPT_MODE, privateKey);
        try {
            recovered = c.doFinal(cipherText);
            throw new Exception("Should throw BadPaddingException");
        } catch (BadPaddingException bpe) {
        }
        c.init(Cipher.DECRYPT_MODE, privateKey, spec2);
        try {
            recovered = c.doFinal(cipherText);
            throw new Exception("Should throw BadPaddingException");
        } catch (BadPaddingException bpe) {
        }
        System.out.println("Testing with cipher default parameters...");
        c.init(Cipher.ENCRYPT_MODE, publicKey);
        cipherText = c.doFinal(plainText);
        AlgorithmParameters params = c.getParameters();
        c.init(Cipher.DECRYPT_MODE, privateKey, params);
        recovered = c.doFinal(cipherText);
        if (Arrays.equals(plainText, recovered) == false) {
            throw new Exception("Decrypted data does not match");
        }
        c.init(Cipher.DECRYPT_MODE, privateKey);
        recovered = c.doFinal(cipherText);
        if (Arrays.equals(plainText, recovered) == false) {
            throw new Exception("Decrypted data does not match");
        }
        c.init(Cipher.DECRYPT_MODE, privateKey, spec2);
        try {
            recovered = c.doFinal(cipherText);
            throw new Exception("Should throw BadPaddingException");
        } catch (BadPaddingException bpe) {
        }
    }
}
