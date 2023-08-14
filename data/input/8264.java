public class TestCipherMode {
    private static final String ALGO = "DES";
    public static void main(String[] argv) throws Exception {
        TestCipherMode test = new TestCipherMode();
        System.out.println("Testing ENCRYPT_MODE...");
        test.checkMode(Cipher.ENCRYPT_MODE);
        System.out.println("Testing DECRYPT_MODE...");
        test.checkMode(Cipher.DECRYPT_MODE);
        System.out.println("Testing WRAP_MODE...");
        test.checkMode(Cipher.WRAP_MODE);
        System.out.println("Testing UNWRAP_MODE...");
        test.checkMode(Cipher.UNWRAP_MODE);
        System.out.println("All Tests Passed");
   }
    private Cipher c = null;
    private SecretKey key = null;
    private TestCipherMode() throws NoSuchAlgorithmException,
    NoSuchProviderException, NoSuchPaddingException {
        c = Cipher.getInstance(ALGO + "/ECB/PKCS5Padding", "SunJCE");
        key = new SecretKeySpec(new byte[8], ALGO);
    }
    private void checkMode(int mode) throws Exception {
        c.init(mode, key);
        switch (mode) {
        case Cipher.ENCRYPT_MODE:
        case Cipher.DECRYPT_MODE:
            try {
                c.wrap(key);
                throw new Exception("ERROR: should throw ISE for wrap()");
            } catch (IllegalStateException ise) {
                System.out.println("expected ISE is thrown for wrap()");
            }
            try {
                c.unwrap(new byte[16], ALGO, Cipher.SECRET_KEY);
                throw new Exception("ERROR: should throw ISE for unwrap()");
            } catch (IllegalStateException ise) {
                System.out.println("expected ISE is thrown for unwrap()");
            }
            break;
        case Cipher.WRAP_MODE:
        case Cipher.UNWRAP_MODE:
            try {
                c.update(new byte[16]);
                throw new Exception("ERROR: should throw ISE for update()");
            } catch (IllegalStateException ise) {
                System.out.println("expected ISE is thrown for update()");
            }
            try {
                c.doFinal();
                throw new Exception("ERROR: should throw ISE for doFinal()");
            } catch (IllegalStateException ise) {
                System.out.println("expected ISE is thrown for doFinal()");
            }
            break;
        }
    }
}
