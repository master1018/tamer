@TestTargetClass(CipherInputStream.class)
public class CipherInputStreamTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Regression test. Checks NullPointerException",
        method = "read",
        args = {byte[].class, int.class, int.class}
    )
    public void testReadBII() throws Exception {
        CipherInputStream stream = new CipherInputStream(null, new NullCipher());
        try {
            stream.read(new byte[1], 1, 0);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Regression test. Checks IllegalStateException",
        method = "close",
        args = {}
    )
    public void testClose() throws Exception {
        try {
            new CipherInputStream(new ByteArrayInputStream(new byte[] { 1 }),
                    Cipher.getInstance("DES/CBC/PKCS5Padding")).close();
            fail("IllegalStateException expected!");
        } catch (IllegalStateException e) {
        }
        try {
            new CipherInputStream(new BufferedInputStream((InputStream) null),
                    Cipher.getInstance("DES/CBC/PKCS5Padding")).close();
            fail("IllegalStateException expected!");
        } catch (IllegalStateException e) {
        }
    }
}
