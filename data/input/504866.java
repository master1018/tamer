@TestTargetClass(NamedNodeMap.class) 
public final class HCNotationsSetNamedItemNS extends DOMTestCase {
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
        notes = "Verifies that setNamedItemNS throws DOMException.",
        method = "setNamedItemNS",
        args = {org.w3c.dom.Node.class}
    )
    public void testNotationsSetNamedItemNS() throws Throwable {
        Document doc;
        NamedNodeMap notations;
        DocumentType docType;
        Element elem;
        doc = (Document) load("hc_staff", builder);
        docType = doc.getDoctype();
        if (!(("text/html".equals(getContentType())))) {
            assertNotNull("docTypeNotNull", docType);
            notations = docType.getNotations();
            assertNotNull("notationsNotNull", notations);
            elem = doc.createElementNS("http:
            try {
                notations.setNamedItemNS(elem);
                fail("throw_HIER_OR_NO_MOD_ERR");
            } catch (DOMException ex) {
                switch (ex.code) {
                case 3:
                    break;
                case 7:
                    break;
                default:
                    throw ex;
                }
            }
        }
    }
}
