public class StringWriterTest extends TestCase {
    @SmallTest
    public void testStringWriter() throws Exception {
        String str = "AbCdEfGhIjKlMnOpQrStUvWxYz";
        StringWriter a = new StringWriter(10);
        a.write(str, 0, 26);
        a.write('X');
        assertEquals("AbCdEfGhIjKlMnOpQrStUvWxYzX", a.toString());
        a.write("alphabravodelta", 5, 5);
        a.append('X');
        assertEquals("AbCdEfGhIjKlMnOpQrStUvWxYzXbravoX", a.toString());
        a.append("omega");
        assertEquals("AbCdEfGhIjKlMnOpQrStUvWxYzXbravoXomega", a.toString());
    }
}
