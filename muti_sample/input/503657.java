@TestTargetClass(NamedNodeMap.class) 
public final class NamedNodeMapGetNamedItemNS extends DOMTestCase {
    DOMDocumentBuilderFactory factory;
    DocumentBuilder builder;
    protected void setUp() throws Exception {
        super.setUp();
        try {
            factory = new DOMDocumentBuilderFactory(DOMDocumentBuilderFactory
                    .getConfiguration2());
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
        method = "getNamedItemNS",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testGetNamedItemNS2() throws Throwable {
        Document doc;
        NamedNodeMap attributes;
        Node element;
        Attr attribute;
        NodeList elementList;
        String attrName;
        doc = (Document) load("staffNS", builder);
        elementList = doc.getElementsByTagNameNS("http:
                "address");
        element = elementList.item(1);
        attributes = element.getAttributes();
        attribute = (Attr) attributes.getNamedItemNS("http:
                "domestic");
        attrName = attribute.getNodeName();
        assertEquals("namednodemapgetnameditemns02", "emp:domestic", attrName);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Doesn't verify DOMException.",
        method = "getNamedItemNS",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testGetNamedItemNS3() throws Throwable {
        Document doc;
        NamedNodeMap attributes;
        Node element;
        Attr attribute;
        Attr newAttr1;
        Attr newAttr2;
        String attrName;
        doc = (Document) load("staffNS", builder);
        element = doc.createElementNS("http:
        newAttr1 = doc.createAttributeNS("http:
        ((Element) element).setAttributeNodeNS(newAttr1);
        newAttr2 = doc.createAttributeNS("http:
        ((Element) element).setAttributeNodeNS(newAttr2);
        attributes = element.getAttributes();
        attribute = (Attr) attributes.getNamedItemNS(
                "http:
        attrName = attribute.getNodeName();
        assertEquals("namednodemapgetnameditemns03", "L2:att", attrName);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Doesn't verify DOMException.",
        method = "getNamedItemNS",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testGetNamedItemNS4() throws Throwable {
        Document doc;
        NamedNodeMap attributes;
        Element element;
        Attr attribute;
        Attr newAttr1;
        NodeList elementList;
        String attrName;
        doc = (Document) load("staffNS", builder);
        elementList = doc.getElementsByTagNameNS("*", "address");
        element = (Element) elementList.item(1);
        newAttr1 = doc.createAttributeNS("http:
        element.setAttributeNodeNS(newAttr1);
        attributes = element.getAttributes();
        attribute = (Attr) attributes.getNamedItemNS(
                "http:
        attrName = attribute.getNodeName();
        assertEquals("namednodemapgetnameditemns04", "street", attrName);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Doesn't verify DOMException.",
        method = "getNamedItemNS",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testGetNamedItemNS5() throws Throwable {
        Document doc;
        NamedNodeMap attributes;
        Node element;
        Attr attribute;
        NodeList elementList;
        doc = (Document) load("staffNS", builder);
        elementList = doc.getElementsByTagNameNS("*", "address");
        element = elementList.item(1);
        attributes = element.getAttributes();
        attribute = (Attr) attributes.getNamedItemNS("*", "street");
        assertNull("namednodemapgetnameditemns05", attribute);
    }
}
