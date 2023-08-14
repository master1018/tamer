@TestTargetClass(Document.class) 
public final class DocumentCreateElementNS extends DOMTestCase {
    DOMDocumentBuilderFactory factory;
    DocumentBuilder builder;
    protected void setUp() throws Exception {
        super.setUp();
        try {
            factory = new DOMDocumentBuilderFactory(DOMDocumentBuilderFactory
                    .getConfiguration1());
            builder = factory.getBuilder();
        } catch (Exception e) {
            fail("Unexpected exception" + e.getMessage());
        }
    }
    protected void tearDown() throws Exception {
        factory = null;
        builder = null;
        super.tearDown();
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies positive functionality.",
        method = "createElementNS",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testCreateElementNS1() throws Throwable {
        Document doc;
        Element element;
        String namespaceURI = "http:
        String qualifiedName = "XML:XML";
        String nodeName;
        String nsURI;
        String localName;
        String prefix;
        String tagName;
        doc = (Document) load("staffNS", builder);
        element = doc.createElementNS(namespaceURI, qualifiedName);
        nodeName = element.getNodeName();
        nsURI = element.getNamespaceURI();
        localName = element.getLocalName();
        prefix = element.getPrefix();
        tagName = element.getTagName();
        assertEquals("documentcreateelementNS01_nodeName", "XML:XML", nodeName);
        assertEquals("documentcreateelementNS01_namespaceURI",
                "http:
        assertEquals("documentcreateelementNS01_localName", "XML", localName);
        assertEquals("documentcreateelementNS01_prefix", "XML", prefix);
        assertEquals("documentcreateelementNS01_tagName", "XML:XML", tagName);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies that createElementNS throws DOMException with INVALID_CHARACTER_ERR code.",
        method = "createElementNS",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testCreateElementNS2() throws Throwable {
        Document doc;
        String namespaceURI = null;
        String qualifiedName = "^^";
        doc = (Document) load("staffNS", builder);
        {
            boolean success = false;
            try {
                doc.createElementNS(namespaceURI, qualifiedName);
            } catch (DOMException ex) {
                success = (ex.code == DOMException.INVALID_CHARACTER_ERR);
            }
            assertTrue("documentcreateelementNS02", success);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies that createElementNS throws DOMException with NAMESPACE_ERR code.",
        method = "createElementNS",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testCreateElementNS5() throws Throwable {
        Document doc;
        String namespaceURI = null;
        String qualifiedName = "null:xml";
        doc = (Document) load("staffNS", builder);
        {
            boolean success = false;
            try {
                doc.createElementNS(namespaceURI, qualifiedName);
            } catch (DOMException ex) {
                success = (ex.code == DOMException.NAMESPACE_ERR);
            }
            assertTrue("documentcreateelementNS05", success);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies that createElementNS throws DOMException with NAMESPACE_ERR code.",
        method = "createElementNS",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testCreateElementNS6() throws Throwable {
        Document doc;
        Document newDoc;
        DocumentType docType = null;
        DOMImplementation domImpl;
        String namespaceURI = "http:
        String qualifiedName = "xml:root";
        doc = (Document) load("staffNS", builder);
        domImpl = doc.getImplementation();
        newDoc = domImpl.createDocument("http:
                "dom:doc", docType);
        {
            boolean success = false;
            try {
                newDoc.createElementNS(namespaceURI, qualifiedName);
            } catch (DOMException ex) {
                success = (ex.code == DOMException.NAMESPACE_ERR);
            }
            assertTrue("documentcreateelementNS06", success);
        }
    }
}
