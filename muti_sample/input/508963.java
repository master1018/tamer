@TestTargetClass(Element.class) 
public final class ElementHasAttribute extends DOMTestCase {
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
        notes = "Verifies hasAttribute method with empty string as a parameter.",
        method = "hasAttribute",
        args = {java.lang.String.class}
    )
    public void testHasAttribute1() throws Throwable {
        Document doc;
        Element element;
        boolean state;
        doc = (Document) load("staff", builder);
        element = doc.getDocumentElement();
        state = element.hasAttribute("");
        assertFalse("elementhasattribute01", state);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies positive functionality.",
        method = "hasAttribute",
        args = {java.lang.String.class}
    )
    public void testHasAttribute3() throws Throwable {
        Document doc;
        Element element;
        boolean state;
        Attr attribute;
        doc = (Document) load("staff", builder);
        element = doc.createElement("address");
        attribute = doc.createAttribute("domestic");
        state = element.hasAttribute("domestic");
        assertFalse("elementhasattribute03_False", state);
        element.setAttributeNode(attribute);
        state = element.hasAttribute("domestic");
        assertTrue("elementhasattribute03_True", state);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies positive functionality.",
        method = "hasAttribute",
        args = {java.lang.String.class}
    )
    public void testHasAttribute4() throws Throwable {
        Document doc;
        Element element;
        boolean state;
        Attr attribute;
        doc = (Document) load("staff", builder);
        element = doc.createElement("address");
        attribute = doc.createAttribute("domestic");
        element.setAttributeNode(attribute);
        state = element.hasAttribute("domestic");
        assertTrue("elementhasattribute04", state);
    }
}
