public class PushbackReaderTest extends TestCase {
    @SmallTest
    public void testPushbackReader() throws Exception {
        String str = "AbCdEfGhIjKlMnOpQrStUvWxYz";
        StringReader aa = new StringReader(str);
        StringReader ba = new StringReader(str);
        StringReader ca = new StringReader(str);
        PushbackReader a = new PushbackReader(aa, 5);
        try {
            a.unread("PUSH".toCharArray());
            assertEquals("PUSHAbCdEfGhIjKlMnOpQrStUvWxYz", IOUtil.read(a));
        } finally {
            a.close();
        }
        PushbackReader b = new PushbackReader(ba, 15);
        try {
            b.unread('X');
            assertEquals("XAbCdEfGhI", IOUtil.read(b, 10));
        } finally {
            b.close();
        }
        PushbackReader c = new PushbackReader(ca);
        try {
            assertEquals("bdfhjlnprtvxz", IOUtil.skipRead(c));
        } finally {
            c.close();
        }
    }
}
