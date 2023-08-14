public class Test4626070 {
    private static final String ALGO = "AES";
    private static final String MODE = "CBC";
    private static final String PADDING = "PKCS5Padding";
    private static final int KEYSIZE = 16; 
    public boolean execute() throws Exception {
        Cipher ci = Cipher.getInstance(ALGO+"/"+MODE+"/"+PADDING, "SunJCE");
        KeyGenerator kg = KeyGenerator.getInstance(ALGO, "SunJCE");
        kg.init(KEYSIZE*8);
        SecretKey key = kg.generateKey();
        AlgorithmParameters params = ci.getParameters();
        ci.init(Cipher.WRAP_MODE, key, params);
        byte[] wrappedKeyEncoding = ci.wrap(key);
        params = ci.getParameters();
        ci.init(Cipher.UNWRAP_MODE, key, params);
        Key recoveredKey = ci.unwrap(wrappedKeyEncoding, "AES",
                                     Cipher.SECRET_KEY);
        if (!key.equals(recoveredKey)) {
            throw new Exception(
                "key after wrap/unwrap is different from the original!");
        }
        return true;
    }
    public static void main (String[] args) throws Exception {
        Security.addProvider(new com.sun.crypto.provider.SunJCE());
        Test4626070 test = new Test4626070();
        String testName = test.getClass().getName() + "[" + ALGO +
            "/" + MODE + "/" + PADDING + "]";
        if (test.execute()) {
            System.out.println(testName + ": Passed!");
        }
    }
}
