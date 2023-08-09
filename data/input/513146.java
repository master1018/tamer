@TestTargetClass(NamedNodeMap.class) 
public final class HCNamedNodeMapInvalidType extends DOMTestCase {
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
        notes = "Verifies that setNamedItem method throws DOMException with HIERARCHY_REQUEST_ERR code.",
        method = "setNamedItem",
        args = {org.w3c.dom.Node.class}
    )
    public void testNamedNodeMapInvalidType() throws Throwable {
        Document doc;
        NamedNodeMap attributes;
        Element docElem;
        Element newElem;
        doc = (Document) load("hc_staff", builder);
        docElem = doc.getDocumentElement();
        attributes = docElem.getAttributes();
        newElem = doc.createElement("html");
        {
            boolean success = false;
            try {
                attributes.setNamedItem(newElem);
            } catch (DOMException ex) {
                success = (ex.code == DOMException.HIERARCHY_REQUEST_ERR);
            }
            assertTrue("throw_HIERARCHY_REQUEST_ERR", success);
        }
    }
}
