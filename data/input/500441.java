@TestTargetClass(Cipher.RSA.class)
public class CipherRSATest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "method",
        args = {}
    )
    public void test_RSAShortKey() {
        CipherRSAThread rsa = new CipherRSAThread("RSA", new int[] {512},
                new String[] {"ECB"}, new String[] {
                        "PKCS1PADDING", "OAEPWITHMD5ANDMGF1PADDING",
                        "OAEPWITHSHA1ANDMGF1PADDING"});
        rsa.launcher();
        assertEquals(rsa.getFailureMessages(), 0, rsa.getTotalFailuresNumber());
    }
    public void disabled_test_RSALongKey() {
        CipherRSAThread rsa = new CipherRSAThread("RSA", new int[] {1024},
                new String[] {"ECB"},
                new String[] {"OAEPWITHSHA-384ANDMGF1PADDING",
                "OAEPWITHSHA-256ANDMGF1PADDING"});
        rsa.launcher();
        assertEquals(rsa.getFailureMessages(), 0, rsa.getTotalFailuresNumber());
    }
   public void disabled_test_RSAXXXLKey() {
        CipherRSAThread rsa = new CipherRSAThread("RSA", new int[] {2048},
                new String[] {"ECB"},
                new String[] {"OAEPWITHSHA-512ANDMGF1PADDING"});
        rsa.launcher();
        assertEquals(rsa.getFailureMessages(), 0, rsa.getTotalFailuresNumber());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "method",
        args = {}
    )
    @AndroidOnly("Fails on RI but succeeds on Android.")
    public void test_RSANoPadding() {
        CipherRSAThread rsa = new CipherRSAThread("RSA", new int[] {1024},
                new String[] {"ECB"}, new String[] {"NOPADDING"});
        rsa.launcher();
        assertEquals(rsa.getFailureMessages(), 0, rsa.getTotalFailuresNumber());
    }
}
