@TestTargetClass(NamedNodeMap.class) 
public final class GetNamedItemNS extends DOMTestCase {
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
        method = "getNamedItem",
        args = {java.lang.String.class}
    )
    public void testGetNamedItemNS1() throws Throwable {
        Document doc;
        NodeList elementList;
        Node testEmployee;
        NamedNodeMap attributes;
        Attr domesticAttr;
        String attrName;
        doc = (Document) load("staffNS", builder);
        elementList = doc.getElementsByTagName("address");
        testEmployee = elementList.item(1);
        attributes = testEmployee.getAttributes();
        domesticAttr = (Attr) attributes.getNamedItemNS("http:
                "domestic");
        attrName = domesticAttr.getNodeName();
        assertEquals("attrName", "dmstc:domestic", attrName);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Doesn't verify DOMException.",
        method = "getNamedItemNS",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testGetNamedItemNS2() throws Throwable {
        String namespaceURI = "http:
        String localName = "domest";
        Document doc;
        NodeList elementList;
        Node testEmployee;
        NamedNodeMap attributes;
        Attr newAttr;
        doc = (Document) load("staffNS", builder);
        elementList = doc.getElementsByTagName("address");
        testEmployee = elementList.item(1);
        attributes = testEmployee.getAttributes();
        newAttr = (Attr) attributes.getNamedItemNS(namespaceURI, localName);
        assertNull("throw_Null", newAttr);
    }
}
