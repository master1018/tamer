@TestTargetClass(NamedNodeMap.class) 
public final class NamedNodeMapRemoveNamedItemNS extends DOMTestCase {
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
        notes = "Doesn't verify DOMException.",
        method = "removeNamedItemNS",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testRemoveNamedItemNS1() throws Throwable {
        Document doc;
        NamedNodeMap attributes;
        Node element;
        Attr attribute;
        NodeList elementList;
        doc = (Document) load("staffNS", builder);
        elementList = doc.getElementsByTagNameNS("http:
                "address");
        element = elementList.item(1);
        attributes = element.getAttributes();
        attribute = (Attr) attributes.removeNamedItemNS("http:
                "domestic");
        attribute = (Attr) attributes.getNamedItemNS("http:
                "domestic");
        assertNull("namednodemapremovenameditemns01", attribute);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify DOMException.",
        method = "removeNamedItemNS",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testRemoveNamedItemNS3() throws Throwable {
        Document doc;
        NamedNodeMap attributes;
        Node element;
        Attr attribute;
        Attr attribute1;
        Attr attribute2;
        String nodeName;
        doc = (Document) load("staffNS", builder);
        element = doc.createElementNS("http:
        attribute1 = doc
                .createAttributeNS("http:
        ((Element) element).setAttributeNodeNS(attribute1);
        attribute2 = doc
                .createAttributeNS("http:
        ((Element) element).setAttributeNodeNS(attribute2);
        attributes = element.getAttributes();
        attribute = (Attr) attributes.removeNamedItemNS(
                "http:
        attribute = (Attr) attributes.getNamedItemNS(
                "http:
        nodeName = attribute.getNodeName();
        assertEquals("namednodemapremovenameditemns02", "L2:att", nodeName);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify DOMException.",
        method = "removeNamedItemNS",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void _testRemoveNamedItemNS4() throws Throwable {
        Document doc;
        NamedNodeMap attributes;
        Node element;
        Attr attribute;
        NodeList elementList;
        doc = (Document) load("staffNS", builder);
        elementList = doc.getElementsByTagNameNS("*", "employee");
        element = elementList.item(0);
        attributes = element.getAttributes();
        attributes.removeNamedItemNS("http:
        attribute = (Attr) attributes.getNamedItemNS(
                "http:
        assertNull("namednodemapremovenameditemns04_1", attribute);
        attributes.removeNamedItemNS("http:
        attribute = (Attr) attributes.getNamedItemNS(
                "http:
        assertNull("namednodemapremovenameditemns04_2", attribute);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies that removeNamedItemNS method throws DOMException with NOT_FOUND_ERR code.",
        method = "removeNamedItemNS",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testRemoveNamedItemNS6() throws Throwable {
        Document doc;
        NamedNodeMap attributes;
        Node element;
        NodeList elementList;
        doc = (Document) load("staffNS", builder);
        elementList = doc.getElementsByTagNameNS("http:
                "employee");
        element = elementList.item(1);
        attributes = element.getAttributes();
        {
            boolean success = false;
            try {
                attributes.removeNamedItemNS("http:
            } catch (DOMException ex) {
                success = (ex.code == DOMException.NOT_FOUND_ERR);
            }
            assertTrue("throw_NOT_FOUND_ERR", success);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies that removeNamedItemNS method throws DOMException with NOT_FOUND_ERR code.",
        method = "removeNamedItemNS",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testRemoveNamedItemNS7() throws Throwable {
        Document doc;
        NamedNodeMap attributes;
        Node element;
        NodeList elementList;
        doc = (Document) load("staffNS", builder);
        elementList = doc.getElementsByTagNameNS("http:
                "employee");
        element = elementList.item(1);
        attributes = element.getAttributes();
        {
            boolean success = false;
            try {
                attributes.removeNamedItemNS("http:
            } catch (DOMException ex) {
                success = (ex.code == DOMException.NOT_FOUND_ERR);
            }
            assertTrue("throw_NOT_FOUND_ERR", success);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies that removeNamedItemNS method throws DOMException with NOT_FOUND_ERR code.",
        method = "removeNamedItemNS",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testRemoveNamedItemNS8() throws Throwable {
        Document doc;
        NamedNodeMap attributes;
        Element element;
        NodeList elementList;
        doc = (Document) load("staffNS", builder);
        elementList = doc.getElementsByTagNameNS("http:
                "address");
        element = (Element) elementList.item(1);
        attributes = element.getAttributes();
        element.removeAttributeNS("http:
        {
            boolean success = false;
            try {
                attributes.removeNamedItemNS("http:
            } catch (DOMException ex) {
                success = (ex.code == DOMException.NOT_FOUND_ERR);
            }
            assertTrue("throw_NOT_FOUND_ERR", success);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify DOMException.",
        method = "removeNamedItemNS",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testRemoveNamedItemNS9() throws Throwable {
        Document doc;
        NamedNodeMap attributes;
        NamedNodeMap newAttributes;
        Element element;
        Attr attribute;
        NodeList elementList;
        doc = (Document) load("staffNS", builder);
        elementList = doc.getElementsByTagNameNS("http:
                "address");
        element = (Element) elementList.item(1);
        attributes = element.getAttributes();
        attribute = (Attr) attributes.removeNamedItemNS("http:
                "domestic");
        newAttributes = element.getAttributes();
        attribute = (Attr) newAttributes.getNamedItemNS("http:
                "domestic");
        assertNull("namednodemapremovenameditemns09", attribute);
    }
}
