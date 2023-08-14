public class ByteArrayInputStreamTest extends TestCase {
    @SmallTest
    public void testByteArrayInputStream() throws Exception {
        String str = "AbCdEfGhIjKlMnOpQrStUvWxYz";
        ByteArrayInputStream a = new ByteArrayInputStream(str.getBytes());
        ByteArrayInputStream b = new ByteArrayInputStream(str.getBytes());
        ByteArrayInputStream c = new ByteArrayInputStream(str.getBytes());
        ByteArrayInputStream d = new ByteArrayInputStream(str.getBytes());
        assertEquals(str, IOUtil.read(a));
        assertEquals("AbCdEfGhIj", IOUtil.read(b, 10));
        assertEquals("bdfhjlnprtvxz", IOUtil.skipRead(c));
        assertEquals("AbCdEfGdEfGhIjKlMnOpQrStUvWxYz", IOUtil.markRead(d, 3, 4));
    }
}
