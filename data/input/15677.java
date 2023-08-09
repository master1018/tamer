public class TestPKCS5PaddingError extends PKCS11Test {
    private static class CI { 
        String transformation;
        String keyAlgo;
        CI(String transformation, String keyAlgo) {
            this.transformation = transformation;
            this.keyAlgo = keyAlgo;
        }
    }
    private static final CI[] TEST_LIST = {
        new CI("DES/CBC/PKCS5Padding", "DES"),
        new CI("DESede/CBC/PKCS5Padding", "DESede"),
        new CI("AES/CBC/PKCS5Padding", "AES"),
        new CI("DES/ECB/PKCS5Padding", "DES"),
        new CI("DESede/ECB/PKCS5Padding", "DESede"),
        new CI("AES/ECB/PKCS5Padding", "AES"),
    };
    private static StringBuffer debugBuf = new StringBuffer();
    public void main(Provider p) throws Exception {
        boolean status = true;
        Random random = new Random();
        try {
            byte[] plainText = new byte[200];
            for (int i = 0; i < TEST_LIST.length; i++) {
                CI currTest = TEST_LIST[i];
                System.out.println("===" + currTest.transformation + "===");
                try {
                    KeyGenerator kg =
                            KeyGenerator.getInstance(currTest.keyAlgo, p);
                    SecretKey key = kg.generateKey();
                    Cipher c1 = Cipher.getInstance(currTest.transformation,
                                                   "SunJCE");
                    c1.init(Cipher.ENCRYPT_MODE, key);
                    byte[] cipherText = c1.doFinal(plainText);
                    AlgorithmParameters params = c1.getParameters();
                    Cipher c2 = Cipher.getInstance(currTest.transformation, p);
                    c2.init(Cipher.DECRYPT_MODE, key, params);
                    if (!p.getName().equals("SunPKCS11-NSS")) {
                        try {
                            System.out.println("Testing with wrong cipherText length");
                            c2.doFinal(cipherText, 0, cipherText.length - 2);
                        } catch (IllegalBlockSizeException ibe) {
                        } catch (Exception ex) {
                            System.out.println("Error: Unexpected Ex " + ex);
                            ex.printStackTrace();
                        }
                    }
                    try {
                        System.out.println("Testing with wrong padding bytes");
                        cipherText[cipherText.length - 1]++;
                        c2.doFinal(cipherText);
                    } catch (BadPaddingException bpe) {
                    } catch (Exception ex) {
                        System.out.println("Error: Unexpected Ex " + ex);
                        ex.printStackTrace();
                    }
                    System.out.println("DONE");
                } catch (NoSuchAlgorithmException nsae) {
                    System.out.println("Skipping unsupported algorithm: " +
                            nsae);
                }
            }
        } catch (Exception ex) {
            if (debugBuf != null) {
                System.out.println(debugBuf.toString());
                debugBuf = new StringBuffer();
            }
            throw ex;
        }
    }
    public static void main(String[] args) throws Exception {
        main(new TestPKCS5PaddingError());
    }
}
