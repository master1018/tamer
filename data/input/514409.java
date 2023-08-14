@TestTargetClass(NamedNodeMap.class) 
public final class RemoveNamedItemNS extends DOMTestCase {
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
        notes = "Doesn't verify DOMException exception.",
        method = "removeNamedItemNS",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testRemoveNamedItemNS1() throws Throwable {
        Document doc;
        NodeList elementList;
        Node testAddress;
        NamedNodeMap attributes;
        Attr newAttr;
        Node removedNode;
        doc = (Document) load("staffNS", builder);
        elementList = doc.getElementsByTagName("address");
        testAddress = elementList.item(1);
        attributes = testAddress.getAttributes();
        removedNode = attributes.removeNamedItemNS("http:
                "domestic");
        assertNotNull("retval", removedNode);
        newAttr = (Attr) attributes.getNamedItem("dmstc:domestic");
        assertNull("nodeRemoved", newAttr);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Verifies DOMException with NOT_FOUND_ERR code.",
        method = "removeNamedItemNS",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testRemoveNamedItemNS2() throws Throwable {
        String namespaceURI = "http:
        String localName = "domest";
        Document doc;
        NodeList elementList;
        Node testAddress;
        NamedNodeMap attributes;
        doc = (Document) load("staffNS", builder);
        elementList = doc.getElementsByTagName("address");
        testAddress = elementList.item(1);
        attributes = testAddress.getAttributes();
        {
            boolean success = false;
            try {
                attributes.removeNamedItemNS(namespaceURI,
                        localName);
            } catch (DOMException ex) {
                success = (ex.code == DOMException.NOT_FOUND_ERR);
            }
            assertTrue("throw_NOT_FOUND_ERR", success);
        }
    }
}
