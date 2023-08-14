public class Test4628062 {
    private static final String ALGO = "AES";
    private static final int[] KEYSIZES =
        { 16, 24, 32 }; 
    public boolean execute() throws Exception {
        KeyGenerator kg = KeyGenerator.getInstance(ALGO, "SunJCE");
        Key keyWithDefaultSize = kg.generateKey();
        byte[] encoding = keyWithDefaultSize.getEncoded();
        if (encoding.length == 0) {
            throw new Exception("default key length is 0!");
        }
        for (int i=0; i<KEYSIZES.length; i++) {
            kg.init(KEYSIZES[i]*8); 
            Key key = kg.generateKey();
            if (key.getEncoded().length != KEYSIZES[i]) {
                throw new Exception("key is generated with the wrong length!");
            }
        }
        try {
            kg.init(KEYSIZES[0]*8+1);
        } catch (InvalidParameterException ex) {
        } catch (Exception ex) {
            throw new Exception("wrong exception is thrown for invalid key size!");
        }
        return true;
    }
    public static void main (String[] args) throws Exception {
        Security.addProvider(new com.sun.crypto.provider.SunJCE());
        Test4628062 test = new Test4628062();
        String testName = test.getClass().getName();
        if (test.execute()) {
            System.out.println(testName + ": Passed!");
        }
    }
}
