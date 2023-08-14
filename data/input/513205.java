public class BufferedInputStreamTest extends TestCase {
    @SmallTest
    public void testBufferedInputStream() throws Exception {
        String str = "AbCdEfGhIjKlM\nOpQrStUvWxYz";
        ByteArrayInputStream aa = new ByteArrayInputStream(str.getBytes());
        ByteArrayInputStream ba = new ByteArrayInputStream(str.getBytes());
        ByteArrayInputStream ca = new ByteArrayInputStream(str.getBytes());
        ByteArrayInputStream da = new ByteArrayInputStream(str.getBytes());
        ByteArrayInputStream ea = new ByteArrayInputStream(str.getBytes());
        BufferedInputStream a = new BufferedInputStream(aa, 6);
        try {
            assertEquals(str, IOUtil.read(a));
        } finally {
            a.close();
        }
        BufferedInputStream b = new BufferedInputStream(ba, 7);
        try {
            assertEquals("AbCdEfGhIj", IOUtil.read(b, 10));
        } finally {
            b.close();
        }
        BufferedInputStream c = new BufferedInputStream(ca, 9);
        try {
            assertEquals("bdfhjl\nprtvxz", IOUtil.skipRead(c));
        } finally {
            c.close();
        }
        BufferedInputStream d = new BufferedInputStream(da, 9);
        try {
            assertEquals('A', d.read());
            d.mark(15);
            assertEquals('b', d.read());
            assertEquals('C', d.read());
            d.reset();
            assertEquals('b', d.read());
        } finally {
            d.close();
        }
        BufferedInputStream e = new BufferedInputStream(ea, 11);
        try {
            assertEquals(str, IOUtil.read(e, 10000));
        } finally {
            e.close();
        }
    }
}
