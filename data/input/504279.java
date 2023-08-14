public class StringReaderTest extends TestCase {
    @SmallTest
    public void testStringReader() throws Exception {
        String str = "AbCdEfGhIjKlMnOpQrStUvWxYz";
        StringReader a = new StringReader(str);
        StringReader b = new StringReader(str);
        StringReader c = new StringReader(str);
        StringReader d = new StringReader(str);
        assertEquals(str, IOUtil.read(a));
        assertEquals("AbCdEfGhIj", IOUtil.read(b, 10));
        assertEquals("bdfhjlnprtvxz", IOUtil.skipRead(c));
        assertEquals("AbCdEfGdEfGhIjKlMnOpQrStUvWxYz", IOUtil.markRead(d, 3, 4));
    }
}
