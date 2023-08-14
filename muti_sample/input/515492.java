@TestTargetClass(Attr.class) 
public final class OwnerElement extends DOMTestCase {
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
        notes = "Verifies positive functionlity.",
        method = "getOwnerElement",
        args = {}
    )
    public void testGetOwnerElement1() throws Throwable {
        Document doc;
        NodeList addressList;
        Node testNode;
        NamedNodeMap attributes;
        Attr domesticAttr;
        Element elementNode;
        String name;
        doc = (Document) load("staff", builder);
        addressList = doc.getElementsByTagName("address");
        testNode = addressList.item(0);
        attributes = testNode.getAttributes();
        domesticAttr = (Attr) attributes.getNamedItem("domestic");
        elementNode = domesticAttr.getOwnerElement();
        name = elementNode.getNodeName();
        assertEquals("throw_Equals", "address", name);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Verifies that getOwnerElement method returns null.",
        method = "getOwnerElement",
        args = {}
    )
    public void testGetOwnerElement2() throws Throwable {
        Document doc;
        Attr newAttr;
        Element elementNode;
        doc = (Document) load("staff", builder);
        newAttr = doc.createAttribute("newAttribute");
        elementNode = newAttr.getOwnerElement();
        assertNull("throw_Null", elementNode);
    }
}
