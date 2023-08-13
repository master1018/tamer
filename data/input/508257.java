@TestTargetClass(Attr.class) 
public final class AttrGetOwnerElement extends DOMTestCase {
    DOMDocumentBuilderFactory factory;
    DocumentBuilder builder;
    protected void setUp() throws Exception {
        super.setUp();
        try {
            factory = new DOMDocumentBuilderFactory(DOMDocumentBuilderFactory
                    .getConfiguration2());
            builder = factory.getBuilder();
        } catch (Exception e) {
            fail("Unexpected exception " + e.getMessage());
        }
    }
    protected void tearDown() throws Exception {
        factory = null;
        builder = null;
        super.tearDown();
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Doesn't verify that getOwnerElement returns null if an attribute is not in use.",
        method = "getOwnerElement",
        args = {}
    )
    public void testGetOwnerElement2() throws Throwable {
        Document doc;
        Element element;
        Element ownerElement;
        String ownerElementName;
        Attr attr;
        doc = (Document) load("staffNS", builder);
        element = doc.createElement("root");
        attr = doc.createAttributeNS("http:
        element.setAttributeNodeNS(attr);
        ownerElement = attr.getOwnerElement();
        ownerElementName = ownerElement.getNodeName();
        assertEquals("attrgetownerelement02", "root", ownerElementName);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Verifies that getOwnerElement returns null if an attribute is not in use.",
        method = "getOwnerElement",
        args = {}
    )
    public void testGetOwnerElement3() throws Throwable {
        Document doc;
        Node ownerElement;
        Attr attr;
        doc = (Document) load("staffNS", builder);
        attr = doc.createAttributeNS("http:
        ownerElement = attr.getOwnerElement();
        assertNull("attrgetownerelement03", ownerElement);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Verifies that getOwnerElement returns null if an attribute is not in use.",
        method = "getOwnerElement",
        args = {}
    )
    public void testGetOwnerElement4() throws Throwable {
        Document doc;
        Document docImp;
        Node ownerElement;
        Element element;
        Attr attr;
        Attr attrImp;
        NodeList addresses;
        doc = (Document) load("staffNS", builder);
        docImp = (Document) load("staff", builder);
        addresses = doc
                .getElementsByTagNameNS("http:
        element = (Element) addresses.item(1);
        assertNotNull("empAddressNotNull", element);
        attr = element.getAttributeNodeNS("http:
        attrImp = (Attr) docImp.importNode(attr, true);
        ownerElement = attrImp.getOwnerElement();
        assertNull("attrgetownerelement04", ownerElement);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Doesn't verify that getOwnerElement returns null if an attribute is not in use.",
        method = "getOwnerElement",
        args = {}
    )
    public void testGetOwnerElement5() throws Throwable {
        Document doc;
        Node element;
        Element ownerElement;
        Element parentElement;
        NodeList elementList;
        String ownerElementName;
        Attr attr;
        NamedNodeMap nodeMap;
        String nullNS = null;
        doc = (Document) load("staffNS", builder);
        elementList = doc.getElementsByTagNameNS("*", "address");
        element = elementList.item(1);
        parentElement = (Element) element.getParentNode();
        nodeMap = element.getAttributes();
        parentElement.removeChild(element);
        attr = (Attr) nodeMap.getNamedItemNS(nullNS, "street");
        ownerElement = attr.getOwnerElement();
        ownerElementName = ownerElement.getNodeName();
        assertEquals("attrgetownerelement05", "address", ownerElementName);
    }
}
