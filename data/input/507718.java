@TestTargetClass(Element.class) 
public final class HasAttribute extends DOMTestCase {
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
        notes = "Verifies that hasAttribute method returns false.",
        method = "hasAttribute",
        args = {java.lang.String.class}
    )
    public void testHasAttribute1() throws Throwable {
        Document doc;
        NodeList elementList;
        Element testNode;
        boolean state;
        doc = (Document) load("staff", builder);
        elementList = doc.getElementsByTagName("address");
        testNode = (Element) elementList.item(4);
        state = testNode.hasAttribute("domestic");
        assertFalse("throw_False", state);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies that hasAttribute method returns false.",
        method = "hasAttribute",
        args = {java.lang.String.class}
    )
    public void testHasAttribute3() throws Throwable {
        Document doc;
        NodeList elementList;
        Element testNode;
        boolean state;
        doc = (Document) load("staff", builder);
        elementList = doc.getElementsByTagName("address");
        testNode = (Element) elementList.item(0);
        state = testNode.hasAttribute("nomatch");
        assertFalse("throw_False", state);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies that hasAttribute method returns true.",
        method = "hasAttribute",
        args = {java.lang.String.class}
    )
    public void testHasAttribute4() throws Throwable {
        Document doc;
        NodeList elementList;
        Element testNode;
        boolean state;
        doc = (Document) load("staffNS", builder);
        elementList = doc.getElementsByTagName("address");
        testNode = (Element) elementList.item(0);
        state = testNode.hasAttribute("dmstc:domestic");
        assertTrue("hasDomesticAttr", state);
    }
}
