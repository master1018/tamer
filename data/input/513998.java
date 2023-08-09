@TestTargetClass(Node.class) 
public final class Prefix extends DOMTestCase {
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
        notes = "Verifies that getPrefix method can return null.",
        method = "getPrefix",
        args = {}
    )
    public void testGetPrefix1() throws Throwable {
        Document doc;
        Node createdNode;
        String prefix;
        doc = (Document) load("staffNS", builder);
        createdNode = doc.createElement("test:employee");
        prefix = createdNode.getPrefix();
        assertNull("throw_Null", prefix);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Verifies positive functionality of getPrefix method.",
        method = "getPrefix",
        args = {}
    )
    public void testGetPrefix2() throws Throwable {
        Document doc;
        NodeList elementList;
        Node testEmployee;
        Node textNode;
        String prefix;
        doc = (Document) load("staffNS", builder);
        elementList = doc.getElementsByTagName("emp:employeeId");
        testEmployee = elementList.item(0);
        assertNotNull("empEmployeeNotNull", testEmployee);
        textNode = testEmployee.getFirstChild();
        prefix = textNode.getPrefix();
        assertNull("textNodePrefix", prefix);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Verifies positive functionality of getPrefix method.",
        method = "getPrefix",
        args = {}
    )
    public void testGetPrefix3() throws Throwable {
        Document doc;
        NodeList elementList;
        Node testEmployee;
        String prefix;
        doc = (Document) load("staffNS", builder);
        elementList = doc.getElementsByTagName("emp:employee");
        testEmployee = elementList.item(0);
        assertNotNull("empEmployeeNotNull", testEmployee);
        prefix = testEmployee.getPrefix();
        assertEquals("prefix", "emp", prefix);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Verifies that getPrefix method returns null.",
        method = "getPrefix",
        args = {}
    )
    public void testGetPrefix4() throws Throwable {
        Document doc;
        NodeList elementList;
        Node testEmployee;
        String prefix;
        doc = (Document) load("staffNS", builder);
        elementList = doc.getElementsByTagName("employee");
        testEmployee = elementList.item(0);
        prefix = testEmployee.getPrefix();
        assertNull("throw_Null", prefix);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Verifies that getPrefix method throws DOMException with NAMESPACE_ERR code.",
        method = "getPrefix",
        args = {}
    )
    public void testGetPrefix5() throws Throwable {
        Document doc;
        NodeList elementList;
        Element addrNode;
        Attr addrAttr;
        doc = (Document) load("staffNS", builder);
        elementList = doc.getElementsByTagName("emp:address");
        addrNode = (Element) elementList.item(0);
        assertNotNull("empAddrNotNull", addrNode);
        addrAttr = addrNode.getAttributeNode("emp:domestic");
        {
            boolean success = false;
            try {
                addrAttr.setPrefix("xmlns");
            } catch (DOMException ex) {
                success = (ex.code == DOMException.NAMESPACE_ERR);
            }
            assertTrue("throw_NAMESPACE_ERR", success);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Verifies that getPrefix method throws DOMException with INVALID_CHARACTER_ERR code.",
        method = "getPrefix",
        args = {}
    )
    public void _testGetPrefix6() throws Throwable {
        Document doc;
        NodeList elementList;
        Node employeeNode;
        doc = (Document) load("staffNS", builder);
        elementList = doc.getElementsByTagName("employee");
        employeeNode = elementList.item(0);
        {
            boolean success = false;
            try {
                employeeNode.setPrefix("pre^fix xmlns='http
            } catch (DOMException ex) {
                success = (ex.code == DOMException.INVALID_CHARACTER_ERR);
            }
            assertTrue("throw_INVALID_CHARACTER_ERR", success);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Verifies that getPrefix method throws DOMException with NAMESPACE_ERR code.",
        method = "getPrefix",
        args = {}
    )
    public void testGetPrefix7() throws Throwable {
        Document doc;
        NodeList elementList;
        Node employeeNode;
        doc = (Document) load("staffNS", builder);
        elementList = doc.getElementsByTagName("employee");
        employeeNode = elementList.item(0);
        {
            boolean success = false;
            try {
                employeeNode.setPrefix("emp::");
            } catch (DOMException ex) {
                success = (ex.code == DOMException.NAMESPACE_ERR);
            }
            assertTrue("throw_NAMESPACE_ERR", success);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Verifies that getPrefix method throws DOMException with NAMESPACE_ERR code.",
        method = "getPrefix",
        args = {}
    )
    public void _testGetPrefix9() throws Throwable {
        Document doc;
        NodeList elementList;
        Element addrNode;
        Attr addrAttr;
        doc = (Document) load("staffNS", builder);
        elementList = doc.getElementsByTagName("address");
        addrNode = (Element) elementList.item(3);
        addrAttr = addrNode.getAttributeNode("xmlns");
        {
            boolean success = false;
            try {
                addrAttr.setPrefix("xxx");
            } catch (DOMException ex) {
                success = (ex.code == DOMException.NAMESPACE_ERR);
            }
            assertTrue("throw_NAMESPACE_ERR", success);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Verifies that getPrefix method throws DOMException with NAMESPACE_ERR code.",
        method = "getPrefix",
        args = {}
    )
    public void testGetPrefix10() throws Throwable {
        Document doc;
        NodeList elementList;
        Node employeeNode;
        doc = (Document) load("staffNS", builder);
        elementList = doc.getElementsByTagName("employee");
        employeeNode = elementList.item(1);
        {
            boolean success = false;
            try {
                employeeNode.setPrefix("xml");
            } catch (DOMException ex) {
                success = (ex.code == DOMException.NAMESPACE_ERR);
            }
            assertTrue("throw_NAMESPACE_ERR", success);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Verifies that getPrefix method throws DOMException with NAMESPACE_ERR code.",
        method = "getPrefix",
        args = {}
    )
    public void testGetPrefix11() throws Throwable {
        Document doc;
        NodeList elementList;
        Node employeeNode;
        doc = (Document) load("staffNS", builder);
        elementList = doc.getElementsByTagName("employee");
        employeeNode = elementList.item(1);
        employeeNode.getNamespaceURI();
        {
            boolean success = false;
            try {
                employeeNode.setPrefix("employee1");
            } catch (DOMException ex) {
                success = (ex.code == DOMException.NAMESPACE_ERR);
            }
            assertTrue("throw_NAMESPACE_ERR", success);
        }
    }
}
