public class ByteArrayOutputStreamTest extends TestCase {
    @SmallTest
    public void testByteArrayOutputStream() throws Exception {
        String str = "AbCdEfGhIjKlMnOpQrStUvWxYz";
        ByteArrayOutputStream a = new ByteArrayOutputStream();
        ByteArrayOutputStream b = new ByteArrayOutputStream(10);
        a.write(str.getBytes(), 0, 26);
        a.write('X');
        a.writeTo(b);
        assertEquals(27, a.size());
        assertEquals("AbCdEfGhIjKlMnOpQrStUvWxYzX", a.toString());
        assertEquals("AbCdEfGhIjKlMnOpQrStUvWxYzX", b.toString());
    }
}
