@TestTargetClass(DocumentType.class) 
public final class DocumentTypeInternalSubset extends DOMTestCase {
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
        notes = "Doesn't check positive case.",
        method = "getInternalSubset",
        args = {}
    )
    public void testGetInternalSubset() throws Throwable {
        Document doc;
        DocumentType docType;
        DOMImplementation domImpl;
        String internal;
        String nullNS = null;
        doc = (Document) load("staffNS", builder);
        domImpl = doc.getImplementation();
        docType = domImpl.createDocumentType("l2:root", nullNS, nullNS);
        internal = docType.getInternalSubset();
        assertNull("internalSubsetNull", internal);
    }
}
