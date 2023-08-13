@TestTargetClass(Attr.class) 
public final class NamespaceURI extends DOMTestCase {
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
        notes = "Doesn't verify that getNamespaceURI method returns null.",
        method = "getNamespaceURI",
        args = {}
    )
    public void testGetNamespaceURI2() throws Throwable {
        Document doc;
        NodeList elementList;
        Element testAddr;
        Attr addrAttr;
        String attrNamespaceURI;
        doc = (Document) load("staffNS", builder);
        elementList = doc.getElementsByTagName("emp:address");
        testAddr = (Element) elementList.item(0);
        assertNotNull("empAddressNotNull", testAddr);
        addrAttr = testAddr.getAttributeNodeNS("http:
                "domestic");
        attrNamespaceURI = addrAttr.getNamespaceURI();
        assertEquals("namespaceURI", "http:
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Doesn't verify that getNamespaceURI method returns null.",
        method = "getNamespaceURI",
        args = {}
    )
    public void testGetNamespaceURI3() throws Throwable {
        Document doc;
        NodeList elementList;
        Node testEmployee;
        String employeeNamespace;
        doc = (Document) load("staffNS", builder);
        elementList = doc.getElementsByTagName("employee");
        testEmployee = elementList.item(0);
        assertNotNull("employeeNotNull", testEmployee);
        employeeNamespace = testEmployee.getNamespaceURI();
        assertEquals("namespaceURI", "http:
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Verifies that getNamespaceURI method returns null.",
        method = "getNamespaceURI",
        args = {}
    )
    public void testGetNamespaceURI4() throws Throwable {
        Document doc;
        NodeList elementList;
        Node testEmployee;
        String employeeNamespace;
        doc = (Document) load("staffNS", builder);
        elementList = doc.getElementsByTagName("employee");
        testEmployee = elementList.item(1);
        employeeNamespace = testEmployee.getNamespaceURI();
        assertNull("throw_Null", employeeNamespace);
    }
}
