@TestTargetClass(DOMImplementation.class) 
public final class DOMImplementationCreateDocumentType extends DOMTestCase {
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
        method = "createDocumentType",
        args = {java.lang.String.class, java.lang.String.class, java.lang.String.class}
    )
    public void testCreateDocumentType1() throws Throwable {
        Document doc;
        DOMImplementation domImpl;
        DocumentType newDocType;
        Document ownerDocument;
        String qualifiedName = "test:root";
        String publicId;
        String systemId;
        List<String> publicIds = new ArrayList<String>();
        publicIds.add("1234");
        publicIds.add("test");
        List<String> systemIds = new ArrayList<String>();
        systemIds.add("");
        systemIds.add("test");
        doc = (Document) load("staffNS", builder);
        domImpl = doc.getImplementation();
        for (int indexN1005D = 0; indexN1005D < publicIds.size(); indexN1005D++) {
            publicId = (String) publicIds.get(indexN1005D);
            for (int indexN10061 = 0; indexN10061 < systemIds.size(); indexN10061++) {
                systemId = (String) systemIds.get(indexN10061);
                newDocType = domImpl.createDocumentType(qualifiedName,
                        publicId, systemId);
                assertNotNull(
                        "domimplementationcreatedocumenttype01_newDocType",
                        newDocType);
                ownerDocument = newDocType.getOwnerDocument();
                assertNull(
                        "domimplementationcreatedocumenttype01_ownerDocument",
                        ownerDocument);
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Doesn't verify DOMException.",
        method = "createDocumentType",
        args = {java.lang.String.class, java.lang.String.class, java.lang.String.class}
    )
    public void testCreateDocumentType2() throws Throwable {
        Document doc;
        DOMImplementation domImpl;
        DocumentType newDocType;
        Document ownerDocument;
        String publicId = "http:
        String systemId = "dom2.dtd";
        String qualifiedName;
        List<String> qualifiedNames = new ArrayList<String>();
        qualifiedNames.add("_:_");
        qualifiedNames.add("_:h0");
        qualifiedNames.add("_:test");
        qualifiedNames.add("_:_.");
        qualifiedNames.add("_:a-");
        qualifiedNames.add("l_:_");
        qualifiedNames.add("ns:_0");
        qualifiedNames.add("ns:a0");
        qualifiedNames.add("ns0:test");
        qualifiedNames.add("ns:EEE.");
        qualifiedNames.add("ns:_-");
        qualifiedNames.add("a.b:c");
        qualifiedNames.add("a-b:c.j");
        qualifiedNames.add("a-b:c");
        doc = (Document) load("staffNS", builder);
        domImpl = doc.getImplementation();
        for (int indexN10077 = 0; indexN10077 < qualifiedNames.size(); indexN10077++) {
            qualifiedName = (String) qualifiedNames.get(indexN10077);
            newDocType = domImpl.createDocumentType(qualifiedName, publicId,
                    systemId);
            assertNotNull("domimplementationcreatedocumenttype02_newDocType",
                    newDocType);
            ownerDocument = newDocType.getOwnerDocument();
            assertNull("domimplementationcreatedocumenttype02_ownerDocument",
                    ownerDocument);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Doesn't verify DOMException.",
        method = "createDocumentType",
        args = {java.lang.String.class, java.lang.String.class, java.lang.String.class}
    )
    public void testCreateDocumentType4() throws Throwable {
        Document doc;
        DOMImplementation domImpl;
        DocumentType newDocType;
        Document ownerDocument;
        String publicId = "http:
        String systemId = "dom2.dtd";
        String qualifiedName;
        List<String> qualifiedNames = new ArrayList<String>();
        qualifiedNames.add("_:_");
        qualifiedNames.add("_:h0");
        qualifiedNames.add("_:test");
        qualifiedNames.add("_:_.");
        qualifiedNames.add("_:a-");
        qualifiedNames.add("l_:_");
        qualifiedNames.add("ns:_0");
        qualifiedNames.add("ns:a0");
        qualifiedNames.add("ns0:test");
        qualifiedNames.add("ns:EEE.");
        qualifiedNames.add("ns:_-");
        qualifiedNames.add("a.b:c");
        qualifiedNames.add("a-b:c.j");
        qualifiedNames.add("a-b:c");
        doc = (Document) load("staffNS", builder);
        domImpl = doc.getImplementation();
        for (int indexN10077 = 0; indexN10077 < qualifiedNames.size(); indexN10077++) {
            qualifiedName = (String) qualifiedNames.get(indexN10077);
            newDocType = domImpl.createDocumentType(qualifiedName, publicId,
                    systemId);
            assertNotNull("domimplementationcreatedocumenttype02_newDocType",
                    newDocType);
            ownerDocument = newDocType.getOwnerDocument();
            assertNull("domimplementationcreatedocumenttype02_ownerDocument",
                    ownerDocument);
        }
    }
}
