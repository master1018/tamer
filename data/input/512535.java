public class LineNumberReaderTest extends TestCase {
    @MediumTest
    public void testLineNumberReader() throws Exception {
        String str = "AbCdEfGhIjKlM\nOpQrStUvWxYz";
        StringReader aa = new StringReader(str);
        StringReader ba = new StringReader(str);
        StringReader ca = new StringReader(str);
        StringReader da = new StringReader(str);
        StringReader ea = new StringReader(str);
        LineNumberReader a = new LineNumberReader(aa);
        try {
            assertEquals(0, a.getLineNumber());
            assertEquals(str, IOUtil.read(a));
            assertEquals(1, a.getLineNumber());
            a.setLineNumber(5);
            assertEquals(5, a.getLineNumber());
        } finally {
            a.close();
        }
        LineNumberReader b = new LineNumberReader(ba);
        try {
            assertEquals("AbCdEfGhIj", IOUtil.read(b, 10));
        } finally {
            b.close();
        }
        LineNumberReader c = new LineNumberReader(ca);
        try {
            assertEquals("bdfhjl\nprtvxz", IOUtil.skipRead(c));
        } finally {
            c.close();
        }
        LineNumberReader d = new LineNumberReader(da);
        try {
            assertEquals("AbCdEfGdEfGhIjKlM\nOpQrStUvWxYz", IOUtil.markRead(d, 3, 4));
        } finally {
            d.close();
        }
        LineNumberReader e = new LineNumberReader(ea);
        try {
            assertEquals("AbCdEfGhIjKlM", e.readLine());
        } finally {
            e.close();
        }
    }
}
