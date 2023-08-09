public class CharArrayReaderTest extends TestCase {
    @SmallTest
    public void testCharArrayReader() throws Exception {
        String str = "AbCdEfGhIjKlMnOpQrStUvWxYz";
        CharArrayReader a = new CharArrayReader(str.toCharArray());
        CharArrayReader b = new CharArrayReader(str.toCharArray());
        CharArrayReader c = new CharArrayReader(str.toCharArray());
        CharArrayReader d = new CharArrayReader(str.toCharArray());
        assertEquals(str, IOUtil.read(a));
        assertEquals("AbCdEfGhIj", IOUtil.read(b, 10));
        assertEquals("bdfhjlnprtvxz", IOUtil.skipRead(c));
        assertEquals("AbCdEfGdEfGhIjKlMnOpQrStUvWxYz", IOUtil.markRead(d, 3, 4));
    }
}
