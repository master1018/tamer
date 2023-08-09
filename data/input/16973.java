public class Test4512524 {
    private static final String ALGO = "AES";
    private static final String MODE = "CBC";
    private static final String PADDING = "NoPadding";
    private static final int KEYSIZE = 16; 
    public boolean execute() throws Exception {
        Cipher ci = Cipher.getInstance(ALGO+"/"+MODE+"/"+PADDING, "SunJCE");
        KeyGenerator kg = KeyGenerator.getInstance(ALGO, "SunJCE");
        kg.init(KEYSIZE*8);
        SecretKey key = kg.generateKey();
        try{
            AlgorithmParameterSpec aps = null;
            ci.init(Cipher.ENCRYPT_MODE, key, aps);
        } catch (NullPointerException ex) {
            throw new Exception("null parameter is not handled correctly!");
        }
        return true;
    }
    public static void main (String[] args) throws Exception {
        Security.addProvider(new com.sun.crypto.provider.SunJCE());
        Test4512524 test = new Test4512524();
        String testName = test.getClass().getName() + "[" + ALGO +
            "/" + MODE + "/" + PADDING + "]";
        if (test.execute()) {
            System.out.println(testName + ": Passed!");
        }
    }
}
