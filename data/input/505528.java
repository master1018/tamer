public class DataOutputStreamTest extends TestCase {
    @SmallTest
    public void testDataOutputStream() throws Exception {
        String str = "AbCdEfGhIjKlMnOpQrStUvWxYz";
        ByteArrayOutputStream aa = new ByteArrayOutputStream();
        DataOutputStream a = new DataOutputStream(aa);
        try {
            a.write(str.getBytes(), 0, 26);
            a.write('A');
            assertEquals(27, aa.size());
            assertEquals("AbCdEfGhIjKlMnOpQrStUvWxYzA", aa.toString());
            a.writeByte('B');
            assertEquals("AbCdEfGhIjKlMnOpQrStUvWxYzAB", aa.toString());
            a.writeBytes("BYTES");
            assertEquals("AbCdEfGhIjKlMnOpQrStUvWxYzABBYTES", aa.toString());
        } finally {
            a.close();
        }
    }
}
