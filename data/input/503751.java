@TestTargetClass(Document.class) 
public final class DocumentGetElementsByTagnameNS extends DOMTestCase {
    DOMDocumentBuilderFactory factory;
    DocumentBuilder builder;
    protected void setUp() throws Exception {
        super.setUp();
        try {
            factory = new DOMDocumentBuilderFactory(DOMDocumentBuilderFactory
                    .getConfiguration2());
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
        notes = "Verifies '*' as parameters.",
        method = "getElementsByTagNameNS",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testGetElementsByTagNameNS1() throws Throwable {
        Document doc;
        Document newDoc;
        DocumentType docType = null;
        DOMImplementation domImpl;
        NodeList childList;
        String nullNS = null;
        doc = (Document) load("staffNS", builder);
        domImpl = doc.getImplementation();
        newDoc = domImpl.createDocument(nullNS, "root", docType);
        childList = newDoc.getElementsByTagNameNS("*", "*");
        assertEquals("documentgetelementsbytagnameNS01", 1, childList
                .getLength());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies '*' as the first parameter.",
        method = "getElementsByTagNameNS",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testGetElementsByTagNameNS2() throws Throwable {
        Document doc;
        Element docElem;
        Element element;
        NodeList childList;
        doc = (Document) load("staffNS", builder);
        docElem = doc.getDocumentElement();
        element = doc.createElementNS("test", "employeeId");
        docElem.appendChild(element);
        childList = doc.getElementsByTagNameNS("*", "employeeId");
        assertEquals("documentgetelementsbytagnameNS02", 6, childList
                .getLength());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies wrong namespaceURI as a parameter.",
        method = "getElementsByTagNameNS",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testGetElementsByTagNameNS3() throws Throwable {
        Document doc;
        NodeList childList;
        doc = (Document) load("staffNS", builder);
        childList = doc.getElementsByTagNameNS("**", "*");
        assertEquals("documentgetelementsbytagnameNS03", 0, childList
                .getLength());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies positive functionality.",
        method = "getElementsByTagNameNS",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testGetElementsByTagNameNS4() throws Throwable {
        Document doc;
        NodeList childList;
        String nullNS = null;
        doc = (Document) load("staffNS", builder);
        childList = doc.getElementsByTagNameNS(nullNS, "0");
        assertEquals("documentgetelementsbytagnameNS04", 0, childList
                .getLength());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies positive functionality.",
        method = "getElementsByTagNameNS",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testGetElementsByTagNameNS5() throws Throwable {
        Document doc;
        NodeList childList;
        doc = (Document) load("staffNS", builder);
        childList = doc.getElementsByTagNameNS("null", "elementId");
        assertEquals("documentgetelementsbytagnameNS05", 0, childList
                .getLength());
    }
}
