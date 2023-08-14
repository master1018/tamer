@TestTargetClass(CipherOutputStream.class)
public class CipherOutputStreamTest extends junit.framework.TestCase {
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Regression test. Checks IllegalStateException.",
        method = "close",
        args = {}
    )
    public void test_close() throws Exception {
        try {
            new CipherOutputStream((OutputStream) null, Cipher
                    .getInstance("DES/CBC/PKCS5Padding")).close();
            fail("IllegalStateException expected");
        } catch (IllegalStateException e) {
        }
        CipherOutputStream ch = new CipherOutputStream((OutputStream) null) {};
        try {
            new CipherOutputStream(ch, Cipher
                    .getInstance("DES/CBC/PKCS5Padding")).close();
            fail("IllegalStateException expected");
        } catch (IllegalStateException e) {
        }
    }
}
