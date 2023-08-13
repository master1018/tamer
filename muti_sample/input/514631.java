@TestTargetClass(Element.class) 
public final class SetAttributeNodeNS extends DOMTestCase {
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
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies DOMException with INUSE_ATTRIBUTE_ERR code.",
        method = "setAttributeNodeNS",
        args = {org.w3c.dom.Attr.class}
    )
    public void testSetAttributeNode1() throws Throwable {
        String namespaceURI = "http:
        String qualifiedName = "emp:newAttr";
        Document doc;
        Element newElement;
        Attr newAttr;
        NodeList elementList;
        Node testAddr;
        doc = (Document) load("staffNS", builder);
        elementList = doc.getElementsByTagName("emp:address");
        testAddr = elementList.item(0);
        assertNotNull("empAddrNotNull", testAddr);
        newElement = doc.createElement("newElement");
        testAddr.appendChild(newElement);
        newAttr = doc.createAttributeNS(namespaceURI, qualifiedName);
        newElement.setAttributeNodeNS(newAttr);
        {
            boolean success = false;
            try {
                ((Element) testAddr).setAttributeNodeNS(newAttr);
            } catch (DOMException ex) {
                success = (ex.code == DOMException.INUSE_ATTRIBUTE_ERR);
            }
            assertTrue("throw_INUSE_ATTRIBUTE_ERR", success);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify DOMException.",
        method = "setAttributeNodeNS",
        args = {org.w3c.dom.Attr.class}
    )
    public void testSetAttributeNode3() throws Throwable {
        String namespaceURI = "http:
        String qualifiedName = "emp:newAttr";
        Document doc;
        NodeList elementList;
        Node testAddr;
        Attr newAttr;
        Attr newAddrAttr;
        doc = (Document) load("staffNS", builder);
        elementList = doc.getElementsByTagName("emp:address");
        testAddr = elementList.item(0);
        assertNotNull("empAddrNotNull", testAddr);
        newAttr = doc.createAttributeNS(namespaceURI, qualifiedName);
        newAddrAttr = ((Element) testAddr)
                .setAttributeNodeNS(newAttr);
        assertNull("throw_Null", newAddrAttr);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify DOMException.",
        method = "setAttributeNodeNS",
        args = {org.w3c.dom.Attr.class}
    )
    public void testSetAttributeNode4() throws Throwable {
        Document doc;
        NodeList elementList;
        Node testAddr;
        Attr newAttr;
        Attr newAddrAttr;
        String newName;
        doc = (Document) load("staffNS", builder);
        elementList = doc.getElementsByTagName("emp:address");
        testAddr = elementList.item(0);
        assertNotNull("empAddrNotNull", testAddr);
        newAttr = doc.createAttributeNS("http:
        newAddrAttr = ((Element) testAddr)
                .setAttributeNodeNS(newAttr);
        newName = newAddrAttr.getNodeName();
        assertEquals("nodeName", "emp:domestic", newName);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies DOMException with WRONG_DOCUMENT_ERR code.",
        method = "setAttributeNodeNS",
        args = {org.w3c.dom.Attr.class}
    )
    public void testSetAttributeNode5() throws Throwable {
        String namespaceURI = "http:
        String qualifiedName = "emp:newAttr";
        Document doc1;
        Document doc2;
        Attr newAttr;
        NodeList elementList;
        Node testAddr;
        doc1 = (Document) load("staffNS", builder);
        doc2 = (Document) load("staffNS", builder);
        newAttr = doc2.createAttributeNS(namespaceURI, qualifiedName);
        elementList = doc1.getElementsByTagName("emp:address");
        testAddr = elementList.item(0);
        {
            boolean success = false;
            try {
                ((Element) testAddr).setAttributeNodeNS(newAttr);
            } catch (DOMException ex) {
                success = (ex.code == DOMException.WRONG_DOCUMENT_ERR);
            }
            assertTrue("throw_WRONG_DOCUMENT_ERR", success);
        }
    }
}
