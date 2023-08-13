@TestTargetClass(DOMImplementation.class) 
public final class DOMImplementationCreateDocument extends DOMTestCase {
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
        notes = "Doesn't verify DOMException.",
        method = "createDocument",
        args = {java.lang.String.class, java.lang.String.class, org.w3c.dom.DocumentType.class}
    )
    public void testCreateDocument3() throws Throwable {
        Document doc;
        DOMImplementation domImpl;
        Document newDoc;
        DocumentType docType = null;
        String namespaceURI = "http:
        String qualifiedName;
        List<String> qualifiedNames = new ArrayList<String>();
        qualifiedNames.add("_:_");
        qualifiedNames.add("_:h0");
        qualifiedNames.add("_:test");
        qualifiedNames.add("l_:_");
        qualifiedNames.add("ns:_0");
        qualifiedNames.add("ns:a0");
        qualifiedNames.add("ns0:test");
        qualifiedNames.add("a.b:c");
        qualifiedNames.add("a-b:c");
        qualifiedNames.add("a-b:c");
        doc = (Document) load("staffNS", builder);
        domImpl = doc.getImplementation();
        for (int indexN1006B = 0; indexN1006B < qualifiedNames.size(); indexN1006B++) {
            qualifiedName = (String) qualifiedNames.get(indexN1006B);
            newDoc = domImpl.createDocument(namespaceURI, qualifiedName,
                    docType);
            assertNotNull("domimplementationcreatedocument03", newDoc);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Verifies DOMException with NAMESPACE_ERR code.",
        method = "createDocument",
        args = {java.lang.String.class, java.lang.String.class, org.w3c.dom.DocumentType.class}
    )
    public void testCreateDocument4() throws Throwable {
        Document doc;
        DOMImplementation domImpl;
        String namespaceURI = null;
        String qualifiedName = "dom:root";
        DocumentType docType = null;
        doc = (Document) load("staffNS", builder);
        domImpl = doc.getImplementation();
        {
            boolean success = false;
            try {
                domImpl.createDocument(namespaceURI, qualifiedName, docType);
            } catch (DOMException ex) {
                success = (ex.code == DOMException.NAMESPACE_ERR);
            }
            assertTrue("domimplementationcreatedocument04", success);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Verifies DOMException with NAMESPACE_ERR code.",
        method = "createDocument",
        args = {java.lang.String.class, java.lang.String.class, org.w3c.dom.DocumentType.class}
    )
    public void testCreateDocument5() throws Throwable {
        Document doc;
        DOMImplementation domImpl;
        String namespaceURI = "http:
        String qualifiedName = "xml:root";
        DocumentType docType = null;
        doc = (Document) load("staffNS", builder);
        domImpl = doc.getImplementation();
        {
            boolean success = false;
            try {
                domImpl.createDocument(namespaceURI, qualifiedName, docType);
            } catch (DOMException ex) {
                success = (ex.code == DOMException.NAMESPACE_ERR);
            }
            assertTrue("domimplementationcreatedocument05", success);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Verifies DOMException with NAMESPACE_ERR code.",
        method = "createDocument",
        args = {java.lang.String.class, java.lang.String.class, org.w3c.dom.DocumentType.class}
    )
    public void testCreateDocument7() throws Throwable {
        Document doc;
        DOMImplementation domImpl;
        String namespaceURI = "http:
        DocumentType docType = null;
        doc = (Document) load("staffNS", builder);
        domImpl = doc.getImplementation();
        {
            boolean success = false;
            try {
                domImpl.createDocument(namespaceURI, ":", docType);
            } catch (DOMException ex) {
                success = (ex.code == DOMException.NAMESPACE_ERR);
            }
            assertTrue("domimplementationcreatedocument07", success);
        }
    }
}
