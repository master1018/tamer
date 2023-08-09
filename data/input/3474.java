public class TestWithoutInit {
    public static void main(String argv[]) throws Exception {
        Cipher ci = new NullCipher();
        byte[] in = new byte[8];
        ci.doFinal(in);
        ci.update(in);
        ci.doFinal(); 
        Key key = new SecretKeySpec(in, "any");
        try {
            ci.wrap(key);
        } catch (UnsupportedOperationException uoe) {
        }
        try {
            ci.unwrap(in, "any", Cipher.SECRET_KEY);
        } catch (UnsupportedOperationException uoe) {
        }
        System.out.println("Test Passed");
    }
}
