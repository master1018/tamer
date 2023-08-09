@TestTargetClass(Element.class) 
public final class ElementRemoveAttributeNS extends DOMTestCase {
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
        method = "removeAttributeNS",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testRemoveAttributeNS() throws Throwable {
        Document doc;
        Element element;
        boolean state;
        Attr attribute;
        doc = (Document) load("staff", builder);
        element = doc.createElementNS("http:
        attribute = doc.createAttributeNS(
                "http:
        element.setAttributeNodeNS(attribute);
        element.removeAttributeNS(
                "http:
        state = element.hasAttributeNS(
                "http:
        assertFalse("elementremoveattributens01", state);
    }
}
