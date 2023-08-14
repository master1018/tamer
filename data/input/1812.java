public class Test4517355 {
    private static final String ALGO = "AES";
    private static final String MODE = "CBC";
    private static final String PADDING = "PKCS5Padding";
    private static final int KEYSIZE = 16; 
    public boolean execute() throws Exception {
        Random rdm = new Random();
        byte[] plainText=new byte[125];
        rdm.nextBytes(plainText);
        Cipher ci = Cipher.getInstance(ALGO+"/"+MODE+"/"+PADDING, "SunJCE");
        KeyGenerator kg = KeyGenerator.getInstance(ALGO, "SunJCE");
        kg.init(KEYSIZE*8);
        SecretKey key = kg.generateKey();
        ci.init(Cipher.ENCRYPT_MODE, key);
        byte[] cipherText = ci.doFinal(plainText);
        byte[] iv = ci.getIV();
        AlgorithmParameterSpec aps = new IvParameterSpec(iv);
        ci.init(Cipher.DECRYPT_MODE, key, aps);
        byte[] recoveredText = new byte[plainText.length];
        try {
            int len = ci.doFinal(cipherText, 0, cipherText.length,
                                 recoveredText);
        } catch (ShortBufferException ex) {
            throw new Exception("output buffer is the right size!");
        }
        if (!Arrays.equals(plainText, recoveredText)) {
            throw new Exception("encryption/decryption does not work!");
        }
        if (Arrays.equals(plainText, cipherText)) {
            throw new Exception("encryption does not work!");
        }
        if ((cipherText.length/16)*16 != cipherText.length) {
            throw new Exception("padding does not work!");
        }
        return true;
    }
    public static void main (String[] args) throws Exception {
        Security.addProvider(new com.sun.crypto.provider.SunJCE());
        Test4517355 test = new Test4517355();
        String testName = test.getClass().getName() + "[" + ALGO +
            "/" + MODE + "/" + PADDING + "]";
        if (test.execute()) {
            System.out.println(testName + ": Passed!");
        }
    }
}
