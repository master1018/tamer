public class BufferedOutputStreamTest extends TestCase {
    @SmallTest
    public void testBufferedOutputStream() throws Exception {
        String str = "AbCdEfGhIjKlMnOpQrStUvWxYz";
        ByteArrayOutputStream aa = new ByteArrayOutputStream();
        BufferedOutputStream a = new BufferedOutputStream(aa, 15);
        try {
            a.write(str.getBytes(), 0, 26);
            a.write('A');
            assertEquals(26, aa.size());
            assertEquals(aa.toString(), str);
            a.flush();
            assertEquals(27, aa.size());
            assertEquals("AbCdEfGhIjKlMnOpQrStUvWxYzA", aa.toString());
        } finally {
            a.close();
        }
    }
}
