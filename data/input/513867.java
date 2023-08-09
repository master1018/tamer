@TestTargetClass(DocumentType.class) 
public final class SystemId extends DOMTestCase {
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
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getSystemId",
        args = {}
    )
    public void testGetSystemId() throws Throwable {
        Document doc;
        DocumentType docType;
        String systemId;
        doc = (Document) load("staffNS", builder);
        docType = doc.getDoctype();
        systemId = docType.getSystemId();
        assertURIEquals("systemId", null, null, null, "staffNS.dtd", null,
                null, null, null, systemId);
    }
}
