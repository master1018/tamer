public class Test4513830 {
    private static final String ALGO = "AES";
    private static final String MODE = "ECB";
    private static final String PADDING = "PKCS5Padding";
    private static final int KEYSIZE = 16; 
    private static final int TEXTLENGTHS[] = {
        16, 17, 18, 19, 20, 21, 22, 23 };
    public boolean execute() throws Exception {
        Random rdm = new Random();
        byte[] plainText=new byte[125];
        rdm.nextBytes(plainText);
        Cipher ci = Cipher.getInstance(ALGO+"/"+MODE+"/"+PADDING, "SunJCE");
        KeyGenerator kg = KeyGenerator.getInstance(ALGO, "SunJCE");
        kg.init(KEYSIZE*8);
        SecretKey key = kg.generateKey();
        ci.init(Cipher.DECRYPT_MODE, key);
        int recoveredTextLength = ci.getOutputSize(16);
        if (recoveredTextLength != 16) {
            throw new Exception("output size should not increase when decrypting!");
        }
        for (int i=0; i<TEXTLENGTHS.length; i++) {
            ci.init(Cipher.ENCRYPT_MODE, key);
            int cipherTextLength = ci.getOutputSize(TEXTLENGTHS[i]);
            if (cipherTextLength != 32) {
                throw new Exception("output size " + cipherTextLength
                                    + " for input size " + TEXTLENGTHS[i]
                                    + " when encrypting is wrong!");
            }
        }
        return true;
    }
    public static void main (String[] args) throws Exception {
        Security.addProvider(new com.sun.crypto.provider.SunJCE());
        Test4513830 test = new Test4513830();
        String testName = test.getClass().getName() + "[" + ALGO +
            "/" + MODE + "/" + PADDING + "]";
        if (test.execute()) {
            System.out.println(testName + ": Passed!");
        }
    }
}
