@TestTargetClass(DOMImplementation.class) 
public final class CreateDocument extends DOMTestCase {
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
        level = TestLevel.PARTIAL,
        notes = "Doesn't verify null as parameters.",
        method = "createDocument",
        args = {java.lang.String.class, java.lang.String.class, org.w3c.dom.DocumentType.class}
    )
    public void testCreateDocument1() throws Throwable {
        String namespaceURI = "http:
        String malformedName = "prefix::local";
        Document doc;
        DocumentType docType = null;
        DOMImplementation domImpl;
        doc = (Document) load("staffNS", builder);
        domImpl = doc.getImplementation();
        boolean success = false;
        try {
            domImpl.createDocument(namespaceURI, malformedName, docType);
        } catch (DOMException ex) {
            success = (ex.code == DOMException.NAMESPACE_ERR);
        }
        assertTrue("throw_NAMESPACE_ERR", success);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Doesn't verify null as parameters.",
        method = "createDocument",
        args = {java.lang.String.class, java.lang.String.class, org.w3c.dom.DocumentType.class}
    )
    public void testCreateDocument2() throws Throwable {
        String namespaceURI = null;
        String qualifiedName = "k:local";
        Document doc;
        DocumentType docType = null;
        DOMImplementation domImpl;
        doc = (Document) load("staffNS", builder);
        domImpl = doc.getImplementation();
        boolean success = false;
        try {
            domImpl.createDocument(namespaceURI, qualifiedName, docType);
        } catch (DOMException ex) {
            success = (ex.code == DOMException.NAMESPACE_ERR);
        }
        assertTrue("throw_NAMESPACE_ERR", success);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Doesn't verify null as parameters.",
        method = "createDocument",
        args = {java.lang.String.class, java.lang.String.class, org.w3c.dom.DocumentType.class}
    )
    public void testCreateDocument5() throws Throwable {
        String namespaceURI = "http:
        String qualifiedName;
        Document doc;
        DocumentType docType = null;
        DOMImplementation domImpl;
        List<String> illegalQNames = new ArrayList<String>();
        illegalQNames.add("namespaceURI:{");
        illegalQNames.add("namespaceURI:}");
        illegalQNames.add("namespaceURI:~");
        illegalQNames.add("namespaceURI:'");
        illegalQNames.add("namespaceURI:!");
        illegalQNames.add("namespaceURI:@");
        illegalQNames.add("namespaceURI:#");
        illegalQNames.add("namespaceURI:$");
        illegalQNames.add("namespaceURI:%");
        illegalQNames.add("namespaceURI:^");
        illegalQNames.add("namespaceURI:&");
        illegalQNames.add("namespaceURI:*");
        illegalQNames.add("namespaceURI:(");
        illegalQNames.add("namespaceURI:)");
        illegalQNames.add("namespaceURI:+");
        illegalQNames.add("namespaceURI:=");
        illegalQNames.add("namespaceURI:[");
        illegalQNames.add("namespaceURI:]");
        illegalQNames.add("namespaceURI:\\");
        illegalQNames.add("namespaceURI:/");
        illegalQNames.add("namespaceURI:;");
        illegalQNames.add("namespaceURI:`");
        illegalQNames.add("namespaceURI:<");
        illegalQNames.add("namespaceURI:>");
        illegalQNames.add("namespaceURI:,");
        illegalQNames.add("namespaceURI:a ");
        illegalQNames.add("namespaceURI:\"");
        doc = (Document) load("staffNS", builder);
        for (int indexN1009A = 0; indexN1009A < illegalQNames.size(); indexN1009A++) {
            qualifiedName = (String) illegalQNames.get(indexN1009A);
            domImpl = doc.getImplementation();
            boolean success = false;
            try {
                domImpl.createDocument(namespaceURI, qualifiedName, docType);
            } catch (DOMException ex) {
                success = (ex.code == DOMException.INVALID_CHARACTER_ERR);
            }
            assertTrue("throw_INVALID_CHARACTER_ERR", success);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Doesn't verify null as parameters.",
        method = "createDocument",
        args = {java.lang.String.class, java.lang.String.class, org.w3c.dom.DocumentType.class}
    )
    public void testCreateDocument6() throws Throwable {
        String namespaceURI = "http:
        String qualifiedName = "xml:local";
        Document doc;
        DocumentType docType = null;
        DOMImplementation domImpl;
        doc = (Document) load("staffNS", builder);
        domImpl = doc.getImplementation();
        boolean success = false;
        try {
            domImpl.createDocument(namespaceURI, qualifiedName, docType);
        } catch (DOMException ex) {
            success = (ex.code == DOMException.NAMESPACE_ERR);
        }
        assertTrue("throw_NAMESPACE_ERR", success);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Doesn't verify null as parameters.",
        method = "createDocument",
        args = {java.lang.String.class, java.lang.String.class, org.w3c.dom.DocumentType.class}
    )
    public void testCreateDocument7() throws Throwable {
        String namespaceURI = "http:
        String qualifiedName = "y:x";
        Document doc;
        DocumentType docType = null;
        DOMImplementation domImpl;
        Document aNewDoc;
        String nodeName;
        String nodeValue;
        doc = (Document) load("staffNS", builder);
        domImpl = doc.getImplementation();
        aNewDoc = domImpl.createDocument(namespaceURI, qualifiedName, docType);
        nodeName = aNewDoc.getNodeName();
        nodeValue = aNewDoc.getNodeValue();
        assertEquals("nodeName", "#document", nodeName);
        assertNull("nodeValue", nodeValue);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Doesn't verify null as parameters.",
        method = "createDocument",
        args = {java.lang.String.class, java.lang.String.class, org.w3c.dom.DocumentType.class}
    )
    public void testCreateDocument8() throws Throwable {
        String namespaceURI = "http:
        DocumentType docType = null;
        DOMImplementation domImpl;
        domImpl = builder.getDOMImplementation();
        try {
            domImpl.createDocument(namespaceURI, "", docType);
            fail();
        } catch (DOMException ex) {
        }
    }
}
