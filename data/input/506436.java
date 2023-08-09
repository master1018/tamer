@TestTargetClass(XMLFilterImpl.class)
public class XMLFilterImplTest extends TestCase {
    private MethodLogger logger = new MethodLogger();
    private MockHandler handler = new MockHandler(logger);
    private XMLFilterImpl parent = new MockFilter(logger);
    private XMLFilterImpl child = new XMLFilterImpl(parent);
    private XMLFilterImpl orphan = new XMLFilterImpl();
    private void assertEquals(Object[] a, Object[] b) {
        assertEquals(a.length, b.length);
        for (int i = 0; i < a.length; i++) {
            assertEquals("Element #" + i + " must be equal", a[i], b[i]);
        }
    }
    public void setUp() {
        parent.setContentHandler(handler);
        parent.setDTDHandler(handler);
        parent.setErrorHandler(handler);
        child.setContentHandler(handler);
        child.setDTDHandler(handler);
        child.setErrorHandler(handler);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "XMLFilterImpl",
        args = { }
    )
    public void testXMLFilterImpl() {
        assertEquals(null, parent.getParent());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "XMLFilterImpl",
        args = { XMLReader.class }
    )
    public void testXMLFilterImplXMLReader() {
        assertEquals(null, parent.getParent());
        XMLFilterImpl filter = new XMLFilterImpl(null);
        assertEquals(null, filter.getParent());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getParent",
            args = { }
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setParent",
            args = { XMLReader.class }
        )
    })
    public void testGetSetParent() {
        child.setParent(null);
        assertEquals(null, child.getParent());
        child.setParent(parent);
        assertEquals(parent, child.getParent());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getFeature",
            args = { String.class }
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setFeature",
            args = { String.class, boolean.class }
        )
    })
    public void testGetSetFeature() {
        try {
            child.setFeature("foo", true);
            assertEquals(true, child.getFeature("foo"));
            child.setFeature("foo", false);
            assertEquals(false, child.getFeature("foo"));
        } catch (SAXNotRecognizedException e) {
            throw new RuntimeException("Unexpected exception", e);
        } catch (SAXNotSupportedException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
        try {
            orphan.setFeature("foo", false);
            fail("SAXNotRecognizedException expected");
        } catch (SAXNotRecognizedException e) {
        } catch (SAXNotSupportedException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getProperty",
            args = { String.class }
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setProperty",
            args = { String.class, Object.class }
        )
    })
    public void testGetSetProperty() {
        try {
            child.setProperty("foo", "bar");
            assertEquals("bar", child.getProperty("foo"));
            child.setProperty("foo", null);
            assertEquals(null, child.getProperty("foo"));
        } catch (SAXNotRecognizedException e) {
            throw new RuntimeException("Unexpected exception", e);
        } catch (SAXNotSupportedException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
        try {
            orphan.setProperty("foo", "bar");
            fail("SAXNotRecognizedException expected");
        } catch (SAXNotRecognizedException e) {
        } catch (SAXNotSupportedException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getEntityResolver",
            args = { }
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setEntityResolver",
            args = { EntityResolver.class }
        )
    })
    public void testGetSetEntityResolver() {
        EntityResolver resolver = new MockResolver();
        parent.setEntityResolver(resolver);
        assertEquals(resolver, parent.getEntityResolver());
        parent.setEntityResolver(null);
        assertEquals(null, parent.getEntityResolver());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getDTDHandler",
            args = { }
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setDTDHandler",
            args = { DTDHandler.class }
        )
    })
    public void testGetSetDTDHandler() {
        parent.setDTDHandler(null);
        assertEquals(null, parent.getDTDHandler());
        parent.setDTDHandler(handler);
        assertEquals(handler, parent.getDTDHandler());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getContentHandler",
            args = { }
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setContentHandler",
            args = { ContentHandler.class }
        )
    })
    public void testGetSetContentHandler() {
        parent.setContentHandler(null);
        assertEquals(null, parent.getContentHandler());
        parent.setContentHandler(handler);
        assertEquals(handler, parent.getContentHandler());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getErrorHandler",
            args = { }
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setErrorHandler",
            args = { ErrorHandler.class }
        )
    })
    public void testGetSetErrorHandler() {
        parent.setErrorHandler(null);
        assertEquals(null, parent.getErrorHandler());
        parent.setErrorHandler(handler);
        assertEquals(handler, parent.getErrorHandler());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "parse",
        args = { InputSource.class }
    )
    public void testParseInputSource() {
        InputSource is = new InputSource();
        try {
            child.parse(is);
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        } catch (IOException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
        assertEquals(1, logger.size());
        assertEquals("parse", logger.getMethod());
        try {
            orphan.parse(is);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        } catch (IOException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "parse",
        args = { String.class }
    )
    public void testParseString() {
        try {
            child.parse("foo");
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        } catch (IOException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
        assertEquals(1, logger.size());
        assertEquals("parse", logger.getMethod());
        try {
            orphan.parse("foo");
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        } catch (IOException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "resolveEntity",
        args = { String.class, String.class }
    )
    public void testResolveEntity() {
        InputSource expected = new InputSource();
        MockResolver resolver = new MockResolver();
        resolver.addEntity("foo", "bar", expected);
        InputSource result = null;
        parent.setEntityResolver(resolver);
        try {
            result = parent.resolveEntity("foo", "bar");
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        } catch (IOException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
        assertEquals(expected, result);
        parent.setEntityResolver(null);
        try {
            result = parent.resolveEntity("foo", "bar");
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        } catch (IOException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
        assertEquals(null, result);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "notationDecl",
        args = { String.class, String.class, String.class }
    )
    public void testNotationDecl() {
        try {
            parent.notationDecl("foo", "bar", "foobar");
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
        assertEquals(logger.size(), 1);
        assertEquals("notationDecl", logger.getMethod());
        assertEquals(new Object[] { "foo", "bar", "foobar" },
                logger.getArgs());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "unparsedEntityDecl",
        args = { String.class, String.class, String.class, String.class }
    )
    public void testUnparsedEntityDecl() {
        try {
            parent.unparsedEntityDecl("foo", "bar", "gabba", "hey");
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
        assertEquals(logger.size(), 1);
        assertEquals("unparsedEntityDecl", logger.getMethod());
        assertEquals(new Object[] { "foo", "bar", "gabba", "hey" },
                logger.getArgs());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setDocumentLocator",
        args = { Locator.class }
    )
    public void testSetDocumentLocator() {
        Locator l = new LocatorImpl();
        child.setDocumentLocator(l);
        assertEquals(logger.size(), 1);
        assertEquals("setDocumentLocator", logger.getMethod());
        assertEquals(new Object[] { l }, logger.getArgs());
        child.setDocumentLocator(null);
        assertEquals(logger.size(), 2);
        assertEquals("setDocumentLocator", logger.getMethod());
        assertEquals(new Object[] { null }, logger.getArgs());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "startDocument",
        args = { }
    )
    public void testStartDocument() {
        try {
            parent.startDocument();
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
            parent.endDocument();
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
        try {
            parent.startPrefixMapping("foo", "http:
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
        assertEquals(logger.size(), 1);
        assertEquals("startPrefixMapping", logger.getMethod());
        assertEquals(new Object[] { "foo", "http:
                logger.getArgs());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "endPrefixMapping",
        args = { String.class }
    )
    public void testEndPrefixMapping() {
        try {
            parent.endPrefixMapping("foo");
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
        assertEquals(logger.size(), 1);
        assertEquals("endPrefixMapping", logger.getMethod());
        assertEquals(new Object[] { "foo" }, logger.getArgs());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "startElement",
        args = { String.class, String.class, String.class, Attributes.class }
    )
    public void testStartElement() {
        Attributes atts = new AttributesImpl();
        try {
            parent.startElement("http:
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
        assertEquals(logger.size(), 1);
        assertEquals("startElement", logger.getMethod());
        assertEquals(new Object[] { "http:
                logger.getArgs());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "endElement",
        args = { String.class, String.class, String.class }
    )
    public void testEndElement() {
        try {
            parent.endElement("http:
         } catch (SAXException e) {
             throw new RuntimeException("Unexpected exception", e);
         }
         assertEquals(logger.size(), 1);
         assertEquals("endElement", logger.getMethod());
         assertEquals(new Object[] { "http:
                 logger.getArgs());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "characters",
        args = { char[].class, int.class, int.class }
    )
    public void testCharacters() {
        char[] ch = "Android".toCharArray();
        try {
            parent.characters(ch, 2, 5);
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
            parent.ignorableWhitespace(ch, 0, 5);
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
            parent.processingInstruction("foo", "bar");
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
        assertEquals(logger.size(), 1);
        assertEquals("processingInstruction", logger.getMethod());
        assertEquals(new Object[] { "foo", "bar" }, logger.getArgs());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "skippedEntity",
        args = { String.class }
    )
    public void testSkippedEntity() {
        try {
            parent.skippedEntity("foo");
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
        assertEquals(logger.size(), 1);
        assertEquals("skippedEntity", logger.getMethod());
        assertEquals(new Object[] { "foo" }, logger.getArgs());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "warning",
        args = { SAXParseException.class }
    )
    public void testWarning() {
        SAXParseException exception = new SAXParseException("Oops!", null);
        try {
            parent.warning(exception);
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
        assertEquals(logger.size(), 1);
        assertEquals("warning", logger.getMethod());
        assertEquals(new Object[] { exception }, logger.getArgs());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "error",
        args = { SAXParseException.class }
    )
    public void testError() {
        SAXParseException exception = new SAXParseException("Oops!", null);
        try {
            parent.error(exception);
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
        assertEquals(logger.size(), 1);
        assertEquals("error", logger.getMethod());
        assertEquals(new Object[] { exception }, logger.getArgs());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "fatalError",
        args = { SAXParseException.class }
    )
    public void testFatalError() {
        SAXParseException exception = new SAXParseException("Oops!", null);
        try {
            parent.fatalError(exception);
        } catch (SAXException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
        assertEquals(logger.size(), 1);
        assertEquals("fatalError", logger.getMethod());
        assertEquals(new Object[] { exception }, logger.getArgs());
    }
}
