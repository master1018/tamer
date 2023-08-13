public class DeclarationTest extends TestCase {
    private String systemIdA;
    private Document documentA;
    private String systemIdB;
    private Document documentB;
    @Override protected void setUp() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        systemIdA = stringToSystemId(
                "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" standalone=\"no\" ?><foo />");
        InputSource inputSourceA = new InputSource(systemIdA);
        inputSourceA.setEncoding("US-ASCII");
        documentA = builder.parse(inputSourceA);
        systemIdB = stringToSystemId(
                "<?xml version=\"1.1\" encoding=\"US-ASCII\" standalone=\"yes\" ?><foo />");
        InputSource inputSourceB = new InputSource(systemIdB);
        inputSourceB.setEncoding("ISO-8859-1");
        documentB = builder.parse(inputSourceB);
    }
    private String stringToSystemId(String contents) throws IOException {
        File file = File.createTempFile("temp", "xml");
        file.deleteOnExit();
        OutputStream out = new FileOutputStream(file);
        out.write(contents.getBytes("UTF-8"));
        out.close();
        return "file:" + file;
    }
    public void testGetInputEncoding() throws Exception {
        assertEquals("US-ASCII", documentA.getInputEncoding());
        assertEquals("ISO-8859-1", documentB.getInputEncoding());
    }
    @KnownFailure("Dalvik doesn't parse the XML declaration")
    public void testGetXmlEncoding() throws Exception {
        String message = "This implementation doesn't parse the encoding from the XML declaration";
        assertEquals(message, "ISO-8859-1", documentA.getXmlEncoding());
        assertEquals(message, "US-ASCII", documentB.getXmlEncoding());
    }
    @KnownFailure("Dalvik doesn't parse the XML declaration")
    public void testGetXmlVersion() throws Exception {
        String message = "This implementation doesn't parse the version from the XML declaration";
        assertEquals(message, "1.0", documentA.getXmlVersion());
        assertEquals(message, "1.1", documentB.getXmlVersion());
    }
    @KnownFailure("Dalvik doesn't parse the XML declaration")
    public void testGetXmlStandalone() throws Exception {
        String message = "This implementation doesn't parse standalone from the XML declaration";
        assertEquals(message, false, documentA.getXmlStandalone());
        assertEquals(message, true, documentB.getXmlStandalone());
    }
    public void testGetDocumentUri() throws Exception {
        assertEquals(systemIdA, documentA.getDocumentURI());
        assertEquals(systemIdB, documentB.getDocumentURI());
    }
}
