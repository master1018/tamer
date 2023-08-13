@TestTargetClass(NamedNodeMap.class) 
public final class HCNotationsRemoveNamedItemNS extends DOMTestCase {
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
        notes = "Verifies that removeNamedItemNS method throws DOMException.",
        method = "removeNamedItemNS",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testRemoveNamedItemNS() throws Throwable {
        Document doc;
        NamedNodeMap notations;
        DocumentType docType;
        doc = (Document) load("hc_staff", builder);
        docType = doc.getDoctype();
        if (!(("text/html".equals(getContentType())))) {
            assertNotNull("docTypeNotNull", docType);
            notations = docType.getNotations();
            assertNotNull("notationsNotNull", notations);
            try {
                notations.removeNamedItemNS("http:
                        "alpha");
                fail("throw_NO_MOD_OR_NOT_FOUND_ERR");
            } catch (DOMException ex) {
                switch (ex.code) {
                case 7:
                    break;
                case 8:
                    break;
                default:
                    throw ex;
                }
            }
        }
    }
}
