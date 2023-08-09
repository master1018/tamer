public class BufferedReaderTest extends TestCase {
    @MediumTest
    public void testBufferedReader() throws Exception {
        String str = "AbCdEfGhIjKlMnOpQrStUvWxYz";
        StringReader aa = new StringReader(str);
        StringReader ba = new StringReader(str);
        StringReader ca = new StringReader(str);
        StringReader da = new StringReader(str);
        BufferedReader a = new BufferedReader(aa, 5);
        try {
            assertEquals(str, IOUtil.read(a));
        } finally {
            a.close();
        }
        BufferedReader b = new BufferedReader(ba, 15);
        try {
            assertEquals("AbCdEfGhIj", IOUtil.read(b, 10));
        } finally {
            b.close();
        }
        BufferedReader c = new BufferedReader(ca);
        try {
            assertEquals("bdfhjlnprtvxz", IOUtil.skipRead(c));
        } finally {
            c.close();
        }
        BufferedReader d = new BufferedReader(da);
        try {
            assertEquals("AbCdEfGdEfGhIjKlMnOpQrStUvWxYz", IOUtil.markRead(d, 3, 4));
        } finally {
            d.close();
        }
    }
}
