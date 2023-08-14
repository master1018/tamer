public class MimeHeaderUnitTests extends TestCase {
    public void testWriteToString() throws Exception {
        MimeHeader header = new MimeHeader();
        String actual1 = header.writeToString();
        assertEquals("empty header", actual1, null);
        header.setHeader("Header1", "value1");
        String actual2 = header.writeToString();
        assertEquals("single header", actual2, "Header1: value1\r\n");
        header.setHeader("Header2", "value2");
        String actual3 = header.writeToString();
        assertEquals("multiple headers", actual3,
                "Header1: value1\r\n"
                + "Header2: value2\r\n");
        header.setHeader(MimeHeader.HEADER_ANDROID_ATTACHMENT_STORE_DATA, "value3");
        String actual4 = header.writeToString();
        assertEquals("multiple headers", actual4,
                "Header1: value1\r\n"
                + "Header2: value2\r\n");
    }
}
