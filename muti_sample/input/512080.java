public class OutputStreamWriterTest extends TestCase {
    @SmallTest
    public void testOutputStreamWriter() throws Exception {
        String str = "AbCdEfGhIjKlMnOpQrStUvWxYz";
        ByteArrayOutputStream aa = new ByteArrayOutputStream();
        OutputStreamWriter a = new OutputStreamWriter(aa, "ISO8859_1");
        try {
            a.write(str, 0, 4);
            a.write('A');
            a.flush();
            assertEquals("ISO8859_1", a.getEncoding());
            assertEquals(5, aa.size());
            assertEquals("AbCdA", aa.toString());
        } finally {
            a.close();
        }
    }
}
