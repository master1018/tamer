@TestTargetClass(Element.class) 
public final class GetAttributeNodeNS extends DOMTestCase {
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
        method = "getAttributeNodeNS",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testGetAttributeNodeNS1() throws Throwable {
        String namespaceURI = "http:
        String localName = "invalidlocalname";
        Document doc;
        NodeList elementList;
        Element testAddr;
        Attr attribute;
        doc = (Document) load("staffNS", builder);
        elementList = doc.getElementsByTagName("emp:address");
        testAddr = (Element) elementList.item(0);
        assertNotNull("empAddrNotNull", testAddr);
        attribute = testAddr.getAttributeNodeNS(namespaceURI, localName);
        assertNull("throw_Null", attribute);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Doesn't verify DOMException.",
        method = "getAttributeNodeNS",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testGetAttributeNodeNS2() throws Throwable {
        Document doc;
        NodeList elementList;
        Element testAddr;
        Attr attribute;
        String attrName;
        doc = (Document) load("staffNS", builder);
        elementList = doc.getElementsByTagName("emp:address");
        testAddr = (Element) elementList.item(0);
        assertNotNull("empAddrNotNull", testAddr);
        attribute = testAddr.getAttributeNodeNS("http:
                "domestic");
        attrName = attribute.getNodeName();
        assertEquals("attrName", "emp:domestic", attrName);
    }
}
