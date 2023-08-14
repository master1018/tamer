public class PushbackInputStreamTest extends TestCase {
    @SmallTest
    public void testPushbackInputStream() throws Exception {
        String str = "AbCdEfGhIjKlM\nOpQrStUvWxYz";
        ByteArrayInputStream aa = new ByteArrayInputStream(str.getBytes());
        ByteArrayInputStream ba = new ByteArrayInputStream(str.getBytes());
        ByteArrayInputStream ca = new ByteArrayInputStream(str.getBytes());
        PushbackInputStream a = new PushbackInputStream(aa, 7);
        try {
            a.unread("push".getBytes());
            assertEquals("pushAbCdEfGhIjKlM\nOpQrStUvWxYz", IOUtil.read(a));
        } finally {
            a.close();
        }
        PushbackInputStream b = new PushbackInputStream(ba, 9);
        try {
            b.unread('X');
            assertEquals("XAbCdEfGhI", IOUtil.read(b, 10));
        } finally {
            b.close();
        }
        PushbackInputStream c = new PushbackInputStream(ca);
        try {
            assertEquals("bdfhjl\nprtvxz", IOUtil.skipRead(c));
        } finally {
            c.close();
        }
    }
}
