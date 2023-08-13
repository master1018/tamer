@TestTargetClass(Element.class) 
public final class SetAttributeNS extends DOMTestCase {
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
        notes = "Verifies DOMException with INVALID_CHARACTER_ERR code.",
        method = "setAttributeNS",
        args = {java.lang.String.class, java.lang.String.class, java.lang.String.class}
    )
    public void testSetAttributeNS1() throws Throwable {
        String namespaceURI = "http:
        String qualifiedName = "emp:qual?name";
        Document doc;
        NodeList elementList;
        Node testAddr;
        doc = (Document) load("staffNS", builder);
        elementList = doc.getElementsByTagName("employee");
        testAddr = elementList.item(0);
        {
            boolean success = false;
            try {
                ((Element) testAddr).setAttributeNS(namespaceURI,
                        qualifiedName, "newValue");
            } catch (DOMException ex) {
                success = (ex.code == DOMException.INVALID_CHARACTER_ERR);
            }
            assertTrue("throw_INVALID_CHARACTER_ERR", success);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies DOMException with NAMESPACE_ERR code.",
        method = "setAttributeNS",
        args = {java.lang.String.class, java.lang.String.class, java.lang.String.class}
    )
    public void testSetAttributeNS2() throws Throwable {
        String namespaceURI = "http:
        String qualifiedName = "emp:";
        Document doc;
        NodeList elementList;
        Node testAddr;
        doc = (Document) load("staffNS", builder);
        elementList = doc.getElementsByTagName("emp:employee");
        testAddr = elementList.item(0);
        {
            try {
                ((Element) testAddr).setAttributeNS(namespaceURI,
                        qualifiedName, "newValue");
                fail();
            } catch (DOMException ex) {
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies positive functionality.",
        method = "setAttributeNS",
        args = {String.class, String.class, String.class}
    )
    public void testSetAttributeNS4() throws Throwable {
        Document doc;
        NodeList elementList;
        Node testAddr;
        Attr addrAttr;
        String resultAttr;
        String resultNamespaceURI;
        String resultLocalName;
        String resultPrefix;
        doc = (Document) load("staffNS", builder);
        elementList = doc.getElementsByTagName("emp:address");
        testAddr = elementList.item(0);
        assertNotNull("empAddrNotNull", testAddr);
        ((Element) testAddr).setAttributeNS("http:
                "newprefix:zone", "newValue");
        addrAttr = ((Element) testAddr).getAttributeNodeNS(
                "http:
        resultAttr = ((Element) testAddr).getAttributeNS(
                "http:
        assertEquals("attrValue", "newValue", resultAttr);
        resultNamespaceURI = addrAttr.getNamespaceURI();
        assertEquals("nsuri", "http:
        resultLocalName = addrAttr.getLocalName();
        assertEquals("lname", "zone", resultLocalName);
        resultPrefix = addrAttr.getPrefix();
        assertEquals("prefix", "newprefix", resultPrefix);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies positive functionality.",
        method = "setAttributeNS",
        args = {java.lang.String.class, java.lang.String.class, java.lang.String.class}
    )
    public void testSetAttributeNS5() throws Throwable {
        String localName = "newAttr";
        String namespaceURI = "http:
        String qualifiedName = "emp:newAttr";
        Document doc;
        NodeList elementList;
        Node testAddr;
        String resultAttr;
        doc = (Document) load("staffNS", builder);
        elementList = doc.getElementsByTagName("emp:address");
        testAddr = elementList.item(0);
        assertNotNull("empAddrNotNull", testAddr);
        ((Element) testAddr).setAttributeNS(namespaceURI,
                qualifiedName, "<newValue>");
        resultAttr = ((Element) testAddr).getAttributeNS(
                namespaceURI, localName);
        assertEquals("throw_Equals", "<newValue>", resultAttr);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies DOMException with NAMESPACE_ERR code.",
        method = "setAttributeNS",
        args = {java.lang.String.class, java.lang.String.class, java.lang.String.class}
    )
    public void testSetAttributeNS6() throws Throwable {
        String namespaceURI = "http:
        String qualifiedName = "xml:qualifiedName";
        Document doc;
        NodeList elementList;
        Node testAddr;
        doc = (Document) load("staffNS", builder);
        elementList = doc.getElementsByTagName("employee");
        testAddr = elementList.item(0);
        {
            boolean success = false;
            try {
                ((Element) testAddr).setAttributeNS(namespaceURI,
                        qualifiedName, "newValue");
            } catch (DOMException ex) {
                success = (ex.code == DOMException.NAMESPACE_ERR);
            }
            assertTrue("throw_NAMESPACE_ERR", success);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies DOMException with NAMESPACE_ERR.",
        method = "setAttributeNS",
        args = {java.lang.String.class, java.lang.String.class, java.lang.String.class}
    )
    public void testSetAttributeNS7() throws Throwable {
        String namespaceURI = "http:
        String qualifiedName = "xmlns";
        Document doc;
        NodeList elementList;
        Node testAddr;
        doc = (Document) load("staffNS", builder);
        elementList = doc.getElementsByTagName("employee");
        testAddr = elementList.item(0);
        {
            boolean success = false;
            try {
                ((Element) testAddr).setAttributeNS(namespaceURI,
                        qualifiedName, "newValue");
            } catch (DOMException ex) {
                success = (ex.code == DOMException.NAMESPACE_ERR);
            }
            assertTrue("throw_NAMESPACE_ERR", success);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies positive functionality.",
        method = "setAttributeNS",
        args = {java.lang.String.class, java.lang.String.class, java.lang.String.class}
    )
    public void testSetAttributeNS9() throws Throwable {
        String localName = "newAttr";
        String namespaceURI = "http:
        String qualifiedName = "emp:newAttr";
        Document doc;
        NodeList elementList;
        Node testAddr;
        Attr addrAttr;
        String resultAttr;
        String resultNamespaceURI;
        String resultLocalName;
        String resultPrefix;
        doc = (Document) load("staffNS", builder);
        elementList = doc.getElementsByTagName("emp:address");
        testAddr = elementList.item(0);
        assertNotNull("empAddrNotNull", testAddr);
        ((Element) testAddr).setAttributeNS(namespaceURI,
                qualifiedName, "newValue");
        addrAttr = ((Element) testAddr).getAttributeNodeNS(
                namespaceURI, localName);
        resultAttr = ((Element) testAddr).getAttributeNS(
                namespaceURI, localName);
        assertEquals("attrValue", "newValue", resultAttr);
        resultNamespaceURI = addrAttr.getNamespaceURI();
        assertEquals("nsuri", "http:
        resultLocalName = addrAttr.getLocalName();
        assertEquals("lname", "newAttr", resultLocalName);
        resultPrefix = addrAttr.getPrefix();
        assertEquals("prefix", "emp", resultPrefix);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies DOMException with NAMESPACE_ERR code.",
        method = "setAttributeNS",
        args = {java.lang.String.class, java.lang.String.class, java.lang.String.class}
    )
    public void testSetAttributeNS10() throws Throwable {
        String namespaceURI = "http:
        Document doc;
        NodeList elementList;
        Node testAddr;
        doc = (Document) load("hc_staff", builder);
        elementList = doc.getElementsByTagName("em");
        testAddr = elementList.item(0);
        {
            try {
                ((Element) testAddr).setAttributeNS(namespaceURI, "",
                        "newValue");
                fail();
            } catch (DOMException ex) {
            }
        }
    }
}
