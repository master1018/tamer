@TestTargetClass(DocumentBuilderFactory.class) 
public class DocumentBuilderFactoryTest extends TestCase {
    DocumentBuilderFactory dbf;
    List<String> cdataElements;
    List<String> textElements;
    List<String> commentElements;
    protected void setUp() throws Exception {
        super.setUp();
        TestEnvironment.reset();
        dbf = DocumentBuilderFactory.newInstance();
        cdataElements = new ArrayList<String>();
        textElements = new ArrayList<String>();
        commentElements = new ArrayList<String>();
    }
    protected void tearDown() throws Exception {
        dbf = null;
        cdataElements = null;
        textElements = null;
        commentElements = null;
        super.tearDown();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "DocumentBuilderFactory",
        args = {}
    )
    public void test_Constructor() {
        try {
            new DocumentBuilderFactoryChild();
        } catch (Exception e) {
            fail("Unexpected exception " + e.toString());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isCoalescing",
        args = {}
    )
    public void test_isCoalescing() {
        dbf.setCoalescing(true);
        assertTrue(dbf.isCoalescing());
        dbf.setCoalescing(false);
        assertFalse(dbf.isCoalescing());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isExpandEntityReferences",
        args = {}
    )
    public void test_isExpandEntityReferences() {
        dbf.setExpandEntityReferences(true);
        assertTrue(dbf.isExpandEntityReferences());
        dbf.setExpandEntityReferences(false);
        assertFalse(dbf.isExpandEntityReferences());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isIgnoringComments",
        args = {}
    )
    public void test_isIgnoringComments() {
        dbf.setIgnoringComments(true);
        assertTrue(dbf.isIgnoringComments());
        dbf.setIgnoringComments(false);
        assertFalse(dbf.isIgnoringComments());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isIgnoringElementContentWhitespace",
        args = {}
    )
    public void test_isIgnoringElementContentWhitespace() {
        dbf.setIgnoringElementContentWhitespace(true);
        assertTrue(dbf.isIgnoringElementContentWhitespace());
        dbf.setIgnoringElementContentWhitespace(false);
        assertFalse(dbf.isIgnoringElementContentWhitespace());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isNamespaceAware",
        args = {}
    )
    public void test_isNamespaceAware() {
        dbf.setNamespaceAware(true);
        assertTrue(dbf.isNamespaceAware());
        dbf.setNamespaceAware(false);
        assertFalse(dbf.isNamespaceAware());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "isValidating",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            notes = "",
            method = "setValidating",
            args = {boolean.class}
        )
    })
    public void test_setIsValidating() {
        dbf.setValidating(true);
        assertTrue(dbf.isValidating());
        dbf.setValidating(false);
        assertFalse(dbf.isValidating());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "isXIncludeAware",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            notes = "",
            method = "setXIncludeAware",
            args = {boolean.class}
        )
    })
    @KnownFailure("Should handle XIncludeAware flag more gracefully")
    public void test_isSetXIncludeAware() {
        dbf.setXIncludeAware(true);
        assertTrue(dbf.isXIncludeAware());
        dbf.setXIncludeAware(false);
        assertFalse(dbf.isXIncludeAware());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "newInstance",
        args = {}
    )
    public void test_newInstance() {
        String className = null;
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            assertNotNull(dbf);
            className = System.getProperty("javax.xml.parsers.DocumentBuilderFactory");
            System.setProperty("javax.xml.parsers.DocumentBuilderFactory",
                    "org.apache.harmony.xml.parsers.DocumentBuilderFactoryImpl");
            dbf = DocumentBuilderFactory.newInstance();
            assertNotNull(dbf);
            assertTrue(dbf instanceof org.apache.harmony.xml.parsers.DocumentBuilderFactoryImpl);
            String keyValuePair = "javax.xml.parsers.DocumentBuilderFactory"
                    + "=" + "org.apache.harmony.xml.parsers.DocumentBuilderFactoryImpl";
            ByteArrayInputStream bis = new ByteArrayInputStream(keyValuePair
                    .getBytes());
            Properties prop = System.getProperties();
            prop.load(bis);
            dbf = DocumentBuilderFactory.newInstance();
            assertNotNull(dbf);
            assertTrue(dbf instanceof org.apache.harmony.xml.parsers.DocumentBuilderFactoryImpl);
            System.setProperty("javax.xml.parsers.DocumentBuilderFactory", "");
            try {
                DocumentBuilderFactory.newInstance();
            } catch (FactoryConfigurationError fce) {
            }
        } catch (Exception e) {
            fail("Unexpected exception " + e.toString());
        } finally {
            if (className == null) {
                System.clearProperty("javax.xml.parsers.DocumentBuilderFactory");
            } else {
                System.setProperty("javax.xml.parsers.DocumentBuilderFactory",
                    className);
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "SAXException untested; unused on Android",
        method = "newDocumentBuilder",
        args = {}
    )
    public void test_newDocumentBuilder() {
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            assertTrue(db instanceof DocumentBuilder);
            db.parse(getClass().getResourceAsStream("/simple.xml"));
        } catch(Exception e) {
            throw new RuntimeException("Unexpected exception", e);
        }
        dbf.setValidating(true);
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
        } catch(ParserConfigurationException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setCoalescing",
        args = {boolean.class}
    )
    public void test_setCoalescingZ() {
        dbf.setCoalescing(true);
        assertTrue(dbf.isCoalescing());
        textElements.clear();
        cdataElements.clear();
        Exception parseException = null;
        DocumentBuilder parser = null;
        try {
            parser = dbf.newDocumentBuilder();
            ValidationErrorHandler errorHandler = new ValidationErrorHandler();
            parser.setErrorHandler(errorHandler);
            Document document = parser.parse(getClass().getResourceAsStream(
                    "/recipt.xml"));
            parseException = errorHandler.getFirstException();
            goThroughDocument((Node) document, "");
            assertTrue(textElements
                    .contains("BeefParmesan<title>withGarlicAngelHairPasta</title>"));
        } catch (Exception ex) {
            parseException = ex;
        }
        parser.setErrorHandler(null);
        if (parseException != null) {
            fail("Unexpected exception " + parseException.getMessage());
        }
        dbf.setCoalescing(false);
        assertFalse(dbf.isCoalescing());
        textElements.clear();
        cdataElements.clear();
        try {
            parser = dbf.newDocumentBuilder();
            ValidationErrorHandler errorHandler = new ValidationErrorHandler();
            parser.setErrorHandler(errorHandler);
            Document document = parser.parse(getClass().getResourceAsStream(
                    "/recipt.xml"));
            parseException = errorHandler.getFirstException();
            goThroughDocument((Node) document, "");
            assertFalse(textElements
                    .contains("BeefParmesan<title>withGarlicAngelHairPasta</title>"));
        } catch (Exception ex) {
            parseException = ex;
        }
        parser.setErrorHandler(null);
        if (parseException != null) {
            fail("Unexpected exception " + parseException.getMessage());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setExpandEntityReferences",
        args = {boolean.class}
    )
    public void test_setExpandEntityReferencesZ() {
        dbf.setExpandEntityReferences(true);
        assertTrue(dbf.isExpandEntityReferences());
        Exception parseException = null;
        DocumentBuilder parser = null;
        try {
            parser = dbf.newDocumentBuilder();
            ValidationErrorHandler errorHandler = new ValidationErrorHandler();
            parser.setErrorHandler(errorHandler);
            Document document = parser.parse(getClass().getResourceAsStream(
                    "/recipt.xml"));
            parseException = errorHandler.getFirstException();
            assertNotNull(document);
        } catch (Exception ex) {
            parseException = ex;
        }
        parser.setErrorHandler(null);
        if (parseException != null) {
            fail("Unexpected exception " + parseException.getMessage());
        }
        dbf.setExpandEntityReferences(false);
        assertFalse(dbf.isExpandEntityReferences());
        try {
            parser = dbf.newDocumentBuilder();
            ValidationErrorHandler errorHandler = new ValidationErrorHandler();
            parser.setErrorHandler(errorHandler);
            Document document = parser.parse(getClass().getResourceAsStream(
                    "/recipt.xml"));
            parseException = errorHandler.getFirstException();
            assertNotNull(document);
        } catch (Exception ex) {
            parseException = ex;
        }
        parser.setErrorHandler(null);
        if (parseException != null) {
            fail("Unexpected exception " + parseException.getMessage());
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getFeature",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "setFeature",
            args = {java.lang.String.class, boolean.class}
        )
    })
    public void test_getSetFeatureLjava_lang_String() {
        String[] features = { "http:
                "http:
        try {
            for (int i = 0; i < features.length; i++) {
                dbf.setFeature(features[i], true);
                assertTrue(dbf.getFeature(features[i]));
            }
        } catch (ParserConfigurationException e) {
            fail("Unexpected ParserConfigurationException" + e.getMessage());
        }
        try {
            for (int i = 0; i < features.length; i++) {
                dbf.setFeature(features[i], false);
                assertFalse(dbf.getFeature(features[i]));
            }
        } catch (ParserConfigurationException e) {
            fail("Unexpected ParserConfigurationException" + e.getMessage());
        }
        try {
            for (int i = 0; i < features.length; i++) {
                dbf.setFeature(null, false);
                fail("NullPointerException expected");
            }
        } catch (NullPointerException e) {
        } catch (ParserConfigurationException e) {
            fail("Unexpected ParserConfigurationException" + e.getMessage());
        }
        String[] badFeatures = { "bad1", "bad2", "" };
        try {
            for (int i = 0; i < badFeatures.length; i++) {
                dbf.setFeature(badFeatures[i], false);
                fail("ParserConfigurationException expected");
            }
        } catch (ParserConfigurationException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setIgnoringComments",
        args = {boolean.class}
    )
    public void test_setIgnoringCommentsZ() {
        commentElements.clear();
        dbf.setIgnoringComments(true);
        assertTrue(dbf.isIgnoringComments());
        try {
            DocumentBuilder parser = dbf.newDocumentBuilder();
            Document document = parser.parse(getClass().getResourceAsStream(
                    "/recipt.xml"));
            goThroughDocument((Node) document, "");
            assertFalse(commentElements.contains("comment1"));
            assertFalse(commentElements.contains("comment2"));
        } catch (IOException e) {
            fail("Unexpected IOException " + e.getMessage());
        } catch (ParserConfigurationException e) {
            fail("Unexpected ParserConfigurationException " + e.getMessage());
        } catch (SAXException e) {
            fail("Unexpected SAXException " + e.getMessage());
        }
        commentElements.clear();
        dbf.setIgnoringComments(false);
        assertFalse(dbf.isIgnoringComments());
        try {
            DocumentBuilder parser = dbf.newDocumentBuilder();
            Document document = parser.parse(getClass().getResourceAsStream(
                    "/recipt.xml"));
            goThroughDocument((Node) document, "");
            assertTrue(commentElements.contains("comment1"));
            assertTrue(commentElements.contains("comment2"));
        } catch (IOException e) {
            fail("Unexpected IOException " + e.getMessage());
        } catch (ParserConfigurationException e) {
            fail("Unexpected ParserConfigurationException " + e.getMessage());
        } catch (SAXException e) {
            fail("Unexpected SAXException " + e.getMessage());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setIgnoringElementContentWhitespace",
        args = {boolean.class}
    )
    public void test_setIgnoringElementContentWhitespaceZ() {
        dbf.setIgnoringElementContentWhitespace(true);
        assertTrue(dbf.isIgnoringElementContentWhitespace());
        try {
            DocumentBuilder parser = dbf.newDocumentBuilder();
            Document document = parser.parse(getClass().getResourceAsStream(
                    "/recipt.xml"));
            assertNotNull(document);
        } catch (IOException e) {
            fail("Unexpected IOException " + e.getMessage());
        } catch (ParserConfigurationException e) {
            fail("Unexpected ParserConfigurationException " + e.getMessage());
        } catch (SAXException e) {
            fail("Unexpected SAXException " + e.getMessage());
        }
        dbf.setIgnoringElementContentWhitespace(false);
        assertFalse(dbf.isIgnoringElementContentWhitespace());
        try {
            DocumentBuilder parser = dbf.newDocumentBuilder();
            Document document = parser.parse(getClass().getResourceAsStream(
                    "/recipt.xml"));
            assertNotNull(document);
        } catch (IOException e) {
            fail("Unexpected IOException " + e.getMessage());
        } catch (ParserConfigurationException e) {
            fail("Unexpected ParserConfigurationException " + e.getMessage());
        } catch (SAXException e) {
            fail("Unexpected SAXException " + e.getMessage());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setNamespaceAware",
        args = {boolean.class}
    )
    public void test_setNamespaceAwareZ() {
        dbf.setNamespaceAware(true);
        assertTrue(dbf.isNamespaceAware());
        try {
            DocumentBuilder parser = dbf.newDocumentBuilder();
            Document document = parser.parse(getClass().getResourceAsStream(
                    "/recipt.xml"));
            assertNotNull(document);
        } catch (IOException e) {
            fail("Unexpected IOException " + e.getMessage());
        } catch (ParserConfigurationException e) {
            fail("Unexpected ParserConfigurationException " + e.getMessage());
        } catch (SAXException e) {
            fail("Unexpected SAXException " + e.getMessage());
        }
        dbf.setNamespaceAware(false);
        assertFalse(dbf.isNamespaceAware());
        try {
            DocumentBuilder parser = dbf.newDocumentBuilder();
            Document document = parser.parse(getClass().getResourceAsStream(
                    "/recipt.xml"));
            assertNotNull(document);
        } catch (IOException e) {
            fail("Unexpected IOException " + e.getMessage());
        } catch (ParserConfigurationException e) {
            fail("Unexpected ParserConfigurationException " + e.getMessage());
        } catch (SAXException e) {
            fail("Unexpected SAXException " + e.getMessage());
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getAttribute",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "setAttribute",
            args = {java.lang.String.class, Object.class}
        )
    })
    public void test_getSetAttribute() {
        try {
            dbf.setAttribute("foo", new Object());
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
        }
        try {
            dbf.getAttribute("foo");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
        }
    }
    private void goThroughDocument(Node node, String indent) {
        String value = node.getNodeValue();
        if (value != null) {
            value = value.replaceAll(" ", "");
            value = value.replaceAll("\n", "");
        }
        switch (node.getNodeType()) {
        case Node.CDATA_SECTION_NODE:
            cdataElements.add(value);
            break;
        case Node.COMMENT_NODE:
            commentElements.add(value);
            break;
        case Node.DOCUMENT_FRAGMENT_NODE:
            break;
        case Node.DOCUMENT_NODE:
            break;
        case Node.DOCUMENT_TYPE_NODE:
            break;
        case Node.ELEMENT_NODE:
            break;
        case Node.ENTITY_NODE:
            break;
        case Node.ENTITY_REFERENCE_NODE:
            break;
        case Node.NOTATION_NODE:
            break;
        case Node.PROCESSING_INSTRUCTION_NODE:
            break;
        case Node.TEXT_NODE:
            textElements.add(value);
            break;
        default:
            break;
        }
        NodeList list = node.getChildNodes();
        for (int i = 0; i < list.getLength(); i++)
            goThroughDocument(list.item(i), indent + "   ");
    }
    private class ValidationErrorHandler implements ErrorHandler {
        private SAXException parseException;
        private int errorCount;
        private int warningCount;
        public ValidationErrorHandler() {
            parseException = null;
            errorCount = 0;
            warningCount = 0;
        }
        public void error(SAXParseException ex) {
            errorCount++;
            if (parseException == null) {
                parseException = ex;
            }
        }
        public void warning(SAXParseException ex) {
            warningCount++;
        }
        public void fatalError(SAXParseException ex) {
            if (parseException == null) {
                parseException = ex;
            }
        }
        public SAXException getFirstException() {
            return parseException;
        }
    }
    private class DocumentBuilderFactoryChild extends DocumentBuilderFactory {
        public DocumentBuilderFactoryChild() {
            super();
        }
        public Object getAttribute(String name) {
            return null;
        }
        public boolean getFeature(String name) {
            return false;
        }
        public DocumentBuilder newDocumentBuilder() {
            return null;
        }
        public void setAttribute(String name, Object value) {
        }
        public void setFeature(String name, boolean value) {
        }
    }
}
