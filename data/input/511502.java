@TestTargetClass(Element.class) 
public final class GetAttributeNS extends DOMTestCase {
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
        method = "getAttributeNS",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testGetAttributeNS2() throws Throwable {
        String namespaceURI = "http:
        String localName = "district";
        String qualifiedName = "emp:district";
        Document doc;
        Attr newAttribute;
        NodeList elementList;
        Element testAddr;
        String attrValue;
        doc = (Document) load("staffNS", builder);
        newAttribute = doc.createAttributeNS(namespaceURI, qualifiedName);
        elementList = doc.getElementsByTagName("emp:address");
        testAddr = (Element) elementList.item(0);
        assertNotNull("empAddrNotNull", testAddr);
        testAddr.setAttributeNodeNS(newAttribute);
        elementList = doc.getElementsByTagName("emp:address");
        testAddr = (Element) elementList.item(0);
        attrValue = testAddr.getAttributeNS(namespaceURI, localName);
        assertEquals("throw_Equals", "", attrValue);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Doesn't verify DOMException.",
        method = "getAttributeNS",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testGetAttributeNS3() throws Throwable {
        String namespaceURI = "http:
        String localName = "domestic";
        Document doc;
        NodeList elementList;
        Element testAddr;
        String attrValue;
        doc = (Document) load("staffNS", builder);
        elementList = doc.getElementsByTagName("emp:address");
        testAddr = (Element) elementList.item(0);
        assertNotNull("empAddrNotNull", testAddr);
        testAddr.removeAttributeNS(namespaceURI, localName);
        attrValue = testAddr.getAttributeNS(namespaceURI, localName);
        assertEquals("throw_Equals", "", attrValue);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Doesn't verify DOMException.",
        method = "getAttributeNS",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testGetAttributeNS4() throws Throwable {
        String namespaceURI = "http:
        String localName = "blank";
        String qualifiedName = "emp:blank";
        Document doc;
        NodeList elementList;
        Element testAddr;
        String attrValue;
        doc = (Document) load("staffNS", builder);
        doc.createAttributeNS(namespaceURI, qualifiedName);
        elementList = doc.getElementsByTagName("emp:address");
        testAddr = (Element) elementList.item(0);
        assertNotNull("empAddrNotNull", testAddr);
        testAddr.setAttributeNS(namespaceURI, qualifiedName, "NewValue");
        attrValue = testAddr.getAttributeNS(namespaceURI, localName);
        assertEquals("throw_Equals", "NewValue", attrValue);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Doesn't verify DOMException.",
        method = "getAttributeNS",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testGetAttributeNS5() throws Throwable {
        Document doc;
        NodeList elementList;
        Element testAddr;
        String attrValue;
        doc = (Document) load("staffNS", builder);
        elementList = doc.getElementsByTagName("emp:address");
        testAddr = (Element) elementList.item(0);
        assertNotNull("empAddrNotNull", testAddr);
        attrValue = testAddr.getAttributeNS("http:
        assertEquals("attrValue", "Yes", attrValue);
    }
}
