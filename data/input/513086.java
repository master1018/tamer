@TestTargetClass(Node.class) 
public final class LocalName extends DOMTestCase {
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
        notes = "Verifies positive functionality.",
        method = "getLocalName",
        args = {}
    )
    public void testGetLocalName1() throws Throwable {
        Document doc;
        NodeList elementList;
        Element testAddr;
        Attr addrAttr;
        String localName;
        doc = (Document) load("staffNS", builder);
        elementList = doc.getElementsByTagName("emp:address");
        testAddr = (Element) elementList.item(0);
        assertNotNull("empAddrNotNull", testAddr);
        addrAttr = testAddr.getAttributeNode("emp:domestic");
        localName = addrAttr.getLocalName();
        assertEquals("localName", "domestic", localName);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies that getLocalName method returns null.",
        method = "getLocalName",
        args = {}
    )
    public void testGetLocalName2() throws Throwable {
        Document doc;
        Node createdNode;
        String localName;
        doc = (Document) load("staffNS", builder);
        createdNode = doc.createElement("test:employee");
        localName = createdNode.getLocalName();
        assertNull("localNameNull", localName);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies that getLocalName method returns null.",
        method = "getLocalName",
        args = {}
    )
    public void testGetLocalName3() throws Throwable {
        Document doc;
        NodeList elementList;
        Node testEmployee;
        Node textNode;
        String localName;
        doc = (Document) load("staffNS", builder);
        elementList = doc.getElementsByTagName("employeeId");
        testEmployee = elementList.item(0);
        textNode = testEmployee.getFirstChild();
        localName = textNode.getLocalName();
        assertNull("textNodeLocalName", localName);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies positive functionality.",
        method = "getLocalName",
        args = {}
    )
    public void testGetLocalName4() throws Throwable {
        Document doc;
        NodeList elementList;
        Node testEmployee;
        String employeeLocalName;
        doc = (Document) load("staffNS", builder);
        elementList = doc.getElementsByTagName("employee");
        testEmployee = elementList.item(0);
        employeeLocalName = testEmployee.getLocalName();
        assertEquals("lname", "employee", employeeLocalName);
    }
}
