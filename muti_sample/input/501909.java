public class BufferedWriterTest extends TestCase {
    @SmallTest
    public void testBufferedWriter() throws Exception {
        String str = "AbCdEfGhIjKlMnOpQrStUvWxYz";
        StringWriter aa = new StringWriter();
        BufferedWriter a = new BufferedWriter(aa, 20);
        try {
            a.write(str.toCharArray(), 0, 26);
            a.write('X');
            a.flush();
            assertEquals("AbCdEfGhIjKlMnOpQrStUvWxYzX", aa.toString());
            a.write("alphabravodelta", 5, 5);
            a.flush();
            assertEquals("AbCdEfGhIjKlMnOpQrStUvWxYzXbravo", aa.toString());
            a.newLine();
            a.write("I'm on a new line.");
            a.flush();
            assertEquals("AbCdEfGhIjKlMnOpQrStUvWxYzXbravo\nI\'m on a new line.", aa.toString());
        } finally {
            a.close();
        }
    }
}
