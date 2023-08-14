@TestTargetClass(DocumentBuilder.class) 
public class DocumentBuilderTest extends TestCase {
    private class MockDocumentBuilder extends DocumentBuilder {
        public MockDocumentBuilder() {
            super();
        }
        @Override
        public DOMImplementation getDOMImplementation() {
            return null;
        }
        @Override
        public boolean isNamespaceAware() {
            return false;
        }
        @Override
        public boolean isValidating() {
            return false;
        }
        @Override
        public Document newDocument() {
            return null;
        }
        @Override
        public Document parse(InputSource is) throws SAXException, IOException {
            return null;
        }
        @Override
        public void setEntityResolver(EntityResolver er) {
        }
        @Override
        public void setErrorHandler(ErrorHandler eh) {
        }
        public Object clone() throws CloneNotSupportedException {
            return super.clone();
        }
    }
    DocumentBuilderFactory dbf;
    DocumentBuilder db;
    protected void setUp() throws Exception {
        TestEnvironment.reset();
        dbf = DocumentBuilderFactory.newInstance();
        dbf.setIgnoringElementContentWhitespace(true);
        db = dbf.newDocumentBuilder();
        super.setUp();
    }
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "DocumentBuilder",
        args = {}
    )
    public void testDocumentBuilder() {
        try {
            new MockDocumentBuilder();
        } catch (Exception e) {
            fail("unexpected exception " + e.toString());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "newDocument",
        args = { }
    )
    public void testNewDocument() {
        Document d;
        try {
            d = dbf.newDocumentBuilder().newDocument();
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception", e);
        }
        assertNotNull(d);
        assertNull(d.getDoctype());
        assertNull(d.getDocumentElement());
        assertNull(d.getNamespaceURI());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getDOMImplementation",
        args = { }
    )
    public void testGetImplementation() {
        DOMImplementation d;
        try {
            d = dbf.newDocumentBuilder().getDOMImplementation();
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception", e);
        }
        assertNotNull(d);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isNamespaceAware",
        args = {}
    )
    public void testIsNamespaceAware() {
        try {
            dbf.setNamespaceAware(true);
            assertTrue(dbf.newDocumentBuilder().isNamespaceAware());
            dbf.setNamespaceAware(false);
            assertFalse(dbf.newDocumentBuilder().isNamespaceAware());
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "No validating parser in Android, hence not tested",
        method = "isValidating",
        args = {}
    )
    public void testIsValidating() {
        try {
            dbf.setValidating(false);
            assertFalse(dbf.newDocumentBuilder().isValidating());
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "No XInclude-aware parser in Android, hence not tested",
        method = "isXIncludeAware",
        args = {}
    )
    @KnownFailure("Should handle XIncludeAware flag more gracefully")
    public void testIsXIncludeAware() {
        try {
            dbf.setXIncludeAware(false);
            assertFalse(dbf.newDocumentBuilder().isXIncludeAware());
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "parse",
        args = {java.io.File.class}
    )
    public void testGetBaseURI() throws IOException, SAXException {
        File f = Support_Resources.resourceToTempFile("/simple.xml");
        Document d = db.parse(f);
        assertTrue(d.getDocumentElement().getBaseURI().startsWith("file:
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "parse",
        args = {java.io.File.class}
    )
    public void test_parseLjava_io_File() throws IOException {
        File f = Support_Resources.resourceToTempFile("/simple.xml");
        try {
            Document d = db.parse(f);
            assertNotNull(d);
            assertEquals(2, d.getChildNodes().getLength());
            assertEquals("#comment",
                    d.getChildNodes().item(0).getNodeName());
            assertEquals("breakfast_menu",
                    d.getChildNodes().item(1).getNodeName());
        } catch (IOException ioe) {
            fail("Unexpected IOException " + ioe.toString());
        } catch (SAXException sax) {
            fail("Unexpected SAXException " + sax.toString());
        }
        try {
            db.parse((File)null);
            fail("Expected IllegalArgumentException was not thrown");
        } catch (IllegalArgumentException iae) {
        } catch (IOException ioe) {
            fail("Unexpected IOException " + ioe.toString());
        } catch (SAXException sax) {
            fail("Unexpected SAXException " + sax.toString());
        }
        try {
            db.parse(new File("_"));
            fail("Expected IOException was not thrown");
        } catch (IOException ioe) {
        } catch (SAXException sax) {
            fail("Unexpected SAXException " + sax.toString());
        }
        f = Support_Resources.resourceToTempFile("/wrong.xml");
        try {
            db.parse(f);
            fail("Expected SAXException was not thrown");
        } catch (IOException ioe) {
            fail("Unexpected IOException " + ioe.toString());
        } catch (SAXException sax) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "parse",
        args = {java.io.InputStream.class}
    )
    public void test_parseLjava_io_InputStream() {
        InputStream is = getClass().getResourceAsStream("/simple.xml");
        try {
            Document d = db.parse(is);
            assertNotNull(d);
            assertEquals(2, d.getChildNodes().getLength());
            assertEquals("#comment",
                    d.getChildNodes().item(0).getNodeName());
            assertEquals("breakfast_menu",
                    d.getChildNodes().item(1).getNodeName());
        } catch (IOException ioe) {
            fail("Unexpected IOException " + ioe.toString());
        } catch (SAXException sax) {
            fail("Unexpected SAXException " + sax.toString());
        }
        try {
            db.parse((InputStream)null);
            fail("Expected IllegalArgumentException was not thrown");
        } catch (IllegalArgumentException iae) {
        } catch (IOException ioe) {
            fail("Unexpected IOException " + ioe.toString());
        } catch (SAXException sax) {
            fail("Unexpected SAXException " + sax.toString());
        }
        try {
            db.parse(new FileInputStream("_"));
            fail("Expected IOException was not thrown");
        } catch (IOException ioe) {
        } catch (SAXException sax) {
            fail("Unexpected SAXException " + sax.toString());
        }
        try {
            is = getClass().getResourceAsStream("/wrong.xml");
            db.parse(is);
            fail("Expected SAXException was not thrown");
        } catch (IOException ioe) {
            fail("Unexpected IOException " + ioe.toString());
        } catch (SAXException sax) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "parse",
        args = { InputSource.class }
    )
    public void testParseInputSource() {
        InputStream stream = getClass().getResourceAsStream("/simple.xml");
        InputSource is = new InputSource(stream);
        try {
            Document d = db.parse(is);
            assertNotNull(d);
            assertEquals(2, d.getChildNodes().getLength());
            assertEquals("#comment",
                    d.getChildNodes().item(0).getNodeName());
            assertEquals("breakfast_menu",
                    d.getChildNodes().item(1).getNodeName());
        } catch (IOException ioe) {
            fail("Unexpected IOException " + ioe.toString());
        } catch (SAXException sax) {
            fail("Unexpected SAXException " + sax.toString());
        }
        try {
            db.parse((InputSource)null);
            fail("Expected IllegalArgumentException was not thrown");
        } catch (IllegalArgumentException iae) {
        } catch (IOException ioe) {
            fail("Unexpected IOException " + ioe.toString());
        } catch (SAXException sax) {
            fail("Unexpected SAXException " + sax.toString());
        }
        try {
            db.parse(new InputSource(new FileInputStream("_")));
            fail("Expected IOException was not thrown");
        } catch (IOException ioe) {
        } catch (SAXException sax) {
            fail("Unexpected SAXException " + sax.toString());
        }
        try {
            is = new InputSource(getClass().getResourceAsStream("/wrong.xml"));
            db.parse(is);
            fail("Expected SAXException was not thrown");
        } catch (IOException ioe) {
            fail("Unexpected IOException " + ioe.toString());
        } catch (SAXException sax) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "parse",
        args = {java.io.InputStream.class, java.lang.String.class}
    )
    public void test_parseLjava_io_InputStreamLjava_lang_String() {
        InputStream is = getClass().getResourceAsStream("/systemid.xml");
        try {
            Document d = db.parse(is, SAXParserTestSupport.XML_SYSTEM_ID);
            assertNotNull(d);
            assertEquals(4, d.getChildNodes().getLength());
            assertEquals("collection",
                    d.getChildNodes().item(0).getNodeName());
            assertEquals("#comment",
                    d.getChildNodes().item(1).getNodeName());
            assertEquals("collection",
                    d.getChildNodes().item(2).getNodeName());
            assertEquals("#comment",
                    d.getChildNodes().item(3).getNodeName());
        } catch (IOException ioe) {
            fail("Unexpected IOException " + ioe.toString());
        } catch (SAXException sax) {
            fail("Unexpected SAXException " + sax.toString());
        }
        try {
            db.parse((InputStream)null, SAXParserTestSupport.XML_SYSTEM_ID);
            fail("Expected IllegalArgumentException was not thrown");
        } catch (IllegalArgumentException iae) {
        } catch (IOException ioe) {
            fail("Unexpected IOException " + ioe.toString());
        } catch (SAXException sax) {
            fail("Unexpected SAXException " + sax.toString());
        }
        try {
            is = getClass().getResourceAsStream("/wrong.xml");
            db.parse(is, SAXParserTestSupport.XML_SYSTEM_ID);
            fail("Expected SAXException was not thrown");
        } catch (IOException ioe) {
            fail("Unexpected IOException " + ioe.toString());
        } catch (SAXException sax) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "parse",
        args = {java.lang.String.class}
    )
    public void test_parseLjava_lang_String() throws Exception {
        URL resource = getClass().getResource("/simple.xml");
        Document d = db.parse(resource.toString());
        assertNotNull(d);
        assertEquals(2, d.getChildNodes().getLength());
        assertEquals("#comment",
                d.getChildNodes().item(0).getNodeName());
        assertEquals("breakfast_menu",
                d.getChildNodes().item(1).getNodeName());
        try {
            db.parse((String)null);
            fail("Expected IllegalArgumentException was not thrown");
        } catch (IllegalArgumentException iae) {
        }
        try {
            db.parse("_");
            fail("Expected IOException was not thrown");
        } catch (IOException ioe) {
        }
        try {
            resource = getClass().getResource("/wrong.xml");
            db.parse(resource.toString());
            fail("Expected SAXException was not thrown");
        } catch (SAXException sax) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "reset",
        args = { }
    )
    @KnownFailure("Android DocumentBuilder should implement reset() properly")
    public void testReset() {
        InputStream source = new ByteArrayInputStream("<a>&foo;</a>".getBytes());
        InputStream entity = new ByteArrayInputStream("bar".getBytes());
        MockResolver resolver = new MockResolver();
        resolver.addEntity("foo", "foo", new InputSource(entity));
        Document d;
        try {
            db = dbf.newDocumentBuilder();
            db.setEntityResolver(resolver);
            db.reset();
            d = db.parse(source);
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception", e);
        }
        Element root = (Element)d.getElementsByTagName("a").item(0);
        assertEquals("foo", ((EntityReference)root.getFirstChild()).getNodeName());
        source = new ByteArrayInputStream("</a>".getBytes());
        MethodLogger logger = new MethodLogger();
        ErrorHandler handler = new MockHandler(logger);
        try {
            db = dbf.newDocumentBuilder();
            db.setErrorHandler(handler);
            db.reset();
            d = db.parse(source);
        } catch (SAXParseException e) {
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception", e);            
        }
        assertEquals(0, logger.size());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setErrorHandler",
        args = { ErrorHandler.class }
    )
    public void testSetErrorHandler() {
        InputStream source = new ByteArrayInputStream("</a>".getBytes());
        MethodLogger logger = new MethodLogger();
        ErrorHandler handler = new MockHandler(logger);
        try {
            db = dbf.newDocumentBuilder();
            db.setErrorHandler(handler);
            db.parse(source);
        } catch (SAXParseException e) {
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception", e);            
        }
        assertEquals("error", logger.getMethod());
        assertTrue(logger.getArgs()[0] instanceof SAXParseException);
        source = new ByteArrayInputStream("</a>".getBytes());
        try {
            db = dbf.newDocumentBuilder();
            db.setErrorHandler(null);
            db.parse(source);
        } catch (SAXParseException e) {
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception", e);            
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setEntityResolver",
        args = { EntityResolver.class }
    )
    @KnownFailure("Android DocumentBuilder should support entity resolving")
    public void testSetEntityResolver() {
        InputStream source = new ByteArrayInputStream("<a>&foo;</a>".getBytes());
        InputStream entity = new ByteArrayInputStream("bar".getBytes());
        MockResolver resolver = new MockResolver();
        resolver.addEntity("foo", "foo", new InputSource(entity));
        Document d;
        try {
            db = dbf.newDocumentBuilder();
            db.setEntityResolver(resolver);
            d = db.parse(source);
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception", e);
        }
        Element root = (Element)d.getElementsByTagName("a").item(0);
        assertEquals("bar", ((Text)root.getFirstChild()).getData());
        source = new ByteArrayInputStream("<a>&foo;</a>".getBytes());
        try {
            db = dbf.newDocumentBuilder();
            db.setEntityResolver(null);
            d = db.parse(source);
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception", e);
        }
        root = (Element)d.getElementsByTagName("a").item(0);
        assertEquals("foo", ((EntityReference)root.getFirstChild()).getNodeName());
    }
}
