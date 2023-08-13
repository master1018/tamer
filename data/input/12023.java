public class Test4511676 {
    private static final String ALGO = "AES";
    private static final int KEYSIZE = 16; 
    public boolean execute() throws Exception {
        Cipher ci = Cipher.getInstance(ALGO, "SunJCE");
        KeyGenerator kg = KeyGenerator.getInstance(ALGO, "SunJCE");
        kg.init(KEYSIZE*8);
        SecretKey key = kg.generateKey();
        try {
            ci.init(Cipher.ENCRYPT_MODE, key);
        } catch (InvalidKeyException ex) {
            throw new Exception("key length is mis-intepreted!");
        }
        return true;
    }
    public static void main (String[] args) throws Exception {
        Security.addProvider(new com.sun.crypto.provider.SunJCE());
        Test4511676 test = new Test4511676();
        String testName = test.getClass().getName() + "[" + ALGO +
            "]";
        if (test.execute()) {
            System.out.println(testName + ": Passed!");
        }
    }
}
