@TestTargetClass(XMLReaderAdapter.class)
public class XMLReaderAdapterTest extends TestCase {
    private MethodLogger logger = new MethodLogger();
    private MockHandler handler = new MockHandler(logger);
    private XMLReader reader = new MockReader(logger);
    private XMLReaderAdapter adapter = new XMLReaderAdapter(reader);
    private void assertEquals(Object[] a, Object[] b) {
        assertEquals(a.length, b.length);
        for (int i = 0; i < a.length; i++) {
            assertEquals("Element #" + i + " must be equal", a[i], b[i]);
        }
    }
    @Override
    public void setUp() {
        TestEnvironment.reset();
        adapter.setDocumentHandler(handler);
        adapter.setDTDHandler(handler);
        adapter.setErrorHandler(handler);
    }
    @Override protected void tearDown() throws Exception {
        TestEnvironment.reset();
        super.tearDown();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "XMLReaderAdapter",
        args = { }
    )
    public void testXMLReaderAdapter() {
        System.setProperty("org.xml.sax.driver",
                "tests.api.org.xml.sax.support.DoNothingXMLReader");
        try {
            new XMLReaderAdapter();
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "XMLReaderAdapter",
        args = { XMLReader.class }
    )
    public void testXMLReaderAdapterXMLReader() {
        @SuppressWarnings("unused")
        XMLReaderAdapter adapter = new XMLReaderAdapter(reader);
        try {
            adapter = new XMLReaderAdapter(null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setLocale",
        args = { Locale.class }
    )
    public void testSetLocale() {
        try {
            adapter.setLocale(Locale.getDefault());
            fail("SAXException expected");
        } catch (SAXException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setEntityResolver",
        args = { EntityResolver.class }
    )
    public void testSetEntityResolver() {
        EntityResolver resolver = new MockResolver();
        adapter.setEntityResolver(resolver);
        assertEquals(resolver, reader.getEntityResolver());
        adapter.setEntityResolver(null);
        assertEquals(null, reader.getEntityResolver());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setDTDHandler",
        args = { DTDHandler.class }
    )
    public void testSetDTDHandler() {
        assertEquals(handler, reader.getDTDHandler());
        adapter.setDTDHandler(null);
        assertEquals(null, reader.getDTDHandler());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setDocumentHandler",
        args = { DocumentHandler.class }
    )
    public void testSetDocumentHandler() {
        try {
            adapter.startDocument();
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
        assertEquals("startDocument", logger.getMethod());
        assertEquals(new Object[] { }, logger.getArgs());
        adapter.setDocumentHandler(null);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setErrorHandler",
        args = { ErrorHandler.class }
    )
    public void testSetErrorHandler() {
        assertEquals(handler, reader.getErrorHandler());
        adapter.setErrorHandler(null);
        assertEquals(null, reader.getErrorHandler());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "parse",
        args = { String.class }
    )
    public void testParseString() {
        try {
            adapter.parse("foo");
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        } catch (IOException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
        assertEquals("parse", logger.getMethod(0));
        assertEquals(InputSource.class, logger.getArgs(0)[0].getClass());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "parse",
        args = { InputSource.class }
    )
    public void testParseInputSource() {
        InputSource source = new InputSource("foo");
        try {
            adapter.parse(source);
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        } catch (IOException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
        assertEquals("parse", logger.getMethod());
        assertEquals(new Object[] { source }, logger.getArgs());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setDocumentLocator",
        args = { Locator.class }
    )
    public void testSetDocumentLocator() {
        LocatorImpl locator = new LocatorImpl();
        adapter.setDocumentLocator(locator);
        assertEquals("setDocumentLocator", logger.getMethod());
        assertEquals(new Object[] { locator }, logger.getArgs());
        adapter.setDocumentHandler(null);
        adapter.setDocumentLocator(locator);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "startDocument",
        args = { }
    )
    public void testStartDocument() {
        try {
            adapter.startDocument();
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
        assertEquals(logger.size(), 1);
        assertEquals("startDocument", logger.getMethod());
        assertEquals(new Object[] {}, logger.getArgs());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "endDocument",
        args = { }
    )
    public void testEndDocument() {
        try {
            adapter.endDocument();
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
        assertEquals(logger.size(), 1);
        assertEquals("endDocument", logger.getMethod());
        assertEquals(new Object[] {}, logger.getArgs());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "startPrefixMapping",
        args = { String.class, String.class }
    )
    public void testStartPrefixMapping() {
        adapter.startPrefixMapping("foo", "http:
        assertEquals(logger.size(), 0);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "endPrefixMapping",
        args = { String.class }
    )
    public void testEndPrefixMapping() {
        adapter.endPrefixMapping("foo");
        assertEquals(logger.size(), 0);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "startElement",
        args = { String.class, String.class, String.class, Attributes.class }
    )
    public void testStartElement() {
        AttributesImpl atts = new AttributesImpl();
        atts.addAttribute("http:
                "int", "42");
        try {
            adapter.startElement("http:
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
        assertEquals(logger.size(), 1);
        assertEquals("startElement", logger.getMethod());
        assertEquals("foo:bar", logger.getArgs()[0]);
        assertEquals("gabba:hey",
                ((AttributeList)logger.getArgs()[1]).getName(0));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "endElement",
        args = { String.class, String.class, String.class }
    )
    public void testEndElement() {
        try {
            adapter.endElement("http:
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
        assertEquals(logger.size(), 1);
        assertEquals("endElement", logger.getMethod());
        assertEquals(new Object[] { "foo:bar" }, logger.getArgs());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "characters",
        args = { char[].class, int.class, int.class }
    )
    public void testCharacters() {
        char[] ch = "Android".toCharArray();
        try {
            adapter.characters(ch, 2, 5);
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
        assertEquals(logger.size(), 1);
        assertEquals("characters", logger.getMethod());
        assertEquals(new Object[] { ch, 2, 5 }, logger.getArgs());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "ignorableWhitespace",
        args = { char[].class, int.class, int.class }
    )
    public void testIgnorableWhitespace() {
        char[] ch = "     ".toCharArray();
        try {
            adapter.ignorableWhitespace(ch, 0, 5);
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
        assertEquals(logger.size(), 1);
        assertEquals("ignorableWhitespace", logger.getMethod());
        assertEquals(new Object[] { ch, 0, 5 }, logger.getArgs());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "processingInstruction",
        args = { String.class, String.class }
    )
    public void testProcessingInstruction() {
        try {
            adapter.processingInstruction("foo", "bar");
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
        assertEquals(logger.size(), 1);
        assertEquals("processingInstruction", logger.getMethod());
        assertEquals(new Object[] { "foo" , "bar" }, logger.getArgs());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "skippedEntity",
        args = { String.class }
    )
    public void testSkippedEntity() {
        try {
            adapter.skippedEntity("foo");
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
        assertEquals(logger.size(), 0);
    }
}
