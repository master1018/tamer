@TestTargetClass(NamedNodeMap.class) 
public final class NamedNodeMapSetNamedItemNS extends DOMTestCase {
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
        method = "setNamedItemNS",
        args = {org.w3c.dom.Node.class}
    )
    public void testSetNamedItemNS1() throws Throwable {
        Document doc;
        NamedNodeMap attributes;
        Node element;
        Attr attribute;
        Attr newAttr1;
        NodeList elementList;
        String attrName;
        doc = (Document) load("staffNS", builder);
        elementList = doc.getElementsByTagNameNS("http:
                "address");
        element = elementList.item(0);
        attributes = element.getAttributes();
        newAttr1 = doc.createAttributeNS("http:
        ((Element) element).setAttributeNodeNS(newAttr1);
        attribute = (Attr) attributes.getNamedItemNS(
                "http:
        attrName = attribute.getNodeName();
        assertEquals("namednodemapsetnameditemns01", "streets", attrName);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Doesn't verify DOMException.",
        method = "setNamedItemNS",
        args = {org.w3c.dom.Node.class}
    )
    public void testSetNamedItemNS2() throws Throwable {
        Document doc;
        NamedNodeMap attributes;
        Element element;
        Attr attribute;
        Attr attribute1;
        String attrName;
        doc = (Document) load("staffNS", builder);
        element = doc.createElementNS("http:
        attribute1 = doc
                .createAttributeNS("http:
        attributes = element.getAttributes();
        attributes.setNamedItemNS(attribute1);
        attribute = (Attr) attributes.getNamedItemNS(
                "http:
        attrName = attribute.getNodeName();
        assertEquals("namednodemapsetnameditemns02", "L1:att", attrName);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Verifies that setNamedItemNS throws DOMException with WRONG_DOCUMENT_ERR code.",
        method = "setNamedItemNS",
        args = {org.w3c.dom.Node.class}
    )
    public void testSetNamedItemNS3() throws Throwable {
        Document doc;
        Document docAlt;
        NamedNodeMap attributes;
        NamedNodeMap attributesAlt;
        NodeList elementList;
        NodeList elementListAlt;
        Element element;
        Element elementAlt;
        Attr attr;
        String nullNS = null;
        doc = (Document) load("staffNS", builder);
        elementList = doc.getElementsByTagNameNS("*", "address");
        element = (Element) elementList.item(1);
        attributes = element.getAttributes();
        docAlt = (Document) load("staffNS", builder);
        elementListAlt = docAlt.getElementsByTagNameNS("*", "address");
        elementAlt = (Element) elementListAlt.item(1);
        attributesAlt = elementAlt.getAttributes();
        attr = (Attr) attributesAlt.getNamedItemNS(nullNS, "street");
        attributesAlt.removeNamedItemNS(nullNS, "street");
        {
            boolean success = false;
            try {
                attributes.setNamedItemNS(attr);
            } catch (DOMException ex) {
                success = (ex.code == DOMException.WRONG_DOCUMENT_ERR);
            }
            assertTrue("throw_WRONG_DOCUMENT_ERR", success);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Verifies that setNamedItemNS throws DOMException with WRONG_DOCUMENT_ERR code.",
        method = "setNamedItemNS",
        args = {org.w3c.dom.Node.class}
    )
    public void testSetNamedItemNS4() throws Throwable {
        Document doc;
        DOMImplementation domImpl;
        Document docAlt; 
        DocumentType docType = null;
        NamedNodeMap attributes;
        NodeList elementList;
        Element element;
        Attr attrAlt;
        String nullNS = null;
        doc = (Document) load("staffNS", builder);
        elementList = doc.getElementsByTagNameNS("*", "address");
        element = (Element) elementList.item(1);
        attributes = element.getAttributes();
        domImpl = doc.getImplementation();
        docAlt = domImpl.createDocument(nullNS, "newDoc", docType);
        attrAlt = docAlt.createAttributeNS(nullNS, "street");
        {
            boolean success = false;
            try {
                attributes.setNamedItemNS(attrAlt);
            } catch (DOMException ex) {
                success = (ex.code == DOMException.WRONG_DOCUMENT_ERR);
            }
            assertTrue("throw_WRONG_DOCUMENT_ERR", success);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Verifies that setNamedItemNS throws DOMException with INUSE_ATTRIBUTE_ERR code.",
        method = "setNamedItemNS",
        args = {org.w3c.dom.Node.class}
    )
    public void testSetNamedItemNS6() throws Throwable {
        Document doc;
        NamedNodeMap attributes;
        NodeList elementList;
        Element element;
        Attr attr;
        doc = (Document) load("staffNS", builder);
        elementList = doc.getElementsByTagNameNS("*", "address");
        element = (Element) elementList.item(0);
        attributes = element.getAttributes();
        attr = (Attr) attributes.getNamedItemNS("http:
                "domestic");
        element = (Element) elementList.item(1);
        attributes = element.getAttributes();
        {
            boolean success = false;
            try {
                attributes.setNamedItemNS(attr);
            } catch (DOMException ex) {
                success = (ex.code == DOMException.INUSE_ATTRIBUTE_ERR);
            }
            assertTrue("namednodemapsetnameditemns06", success);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Verifies that setNamedItemNS throws DOMException with INUSE_ATTRIBUTE_ERR code.",
        method = "setNamedItemNS",
        args = {org.w3c.dom.Node.class}
    )
    public void testSetNamedItemNS7() throws Throwable {
        Document doc;
        NamedNodeMap attributes;
        NodeList elementList;
        Element element;
        Attr attr;
        doc = (Document) load("staffNS", builder);
        elementList = doc.getElementsByTagNameNS("*", "address");
        element = (Element) elementList.item(0);
        attributes = element.getAttributes();
        attr = (Attr) attributes.getNamedItemNS("http:
                "domestic");
        element = (Element) elementList.item(1);
        attributes = element.getAttributes();
        {
            boolean success = false;
            try {
                attributes.setNamedItemNS(attr);
            } catch (DOMException ex) {
                success = (ex.code == DOMException.INUSE_ATTRIBUTE_ERR);
            }
            assertTrue("namednodemapsetnameditemns07", success);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Verifies that setNamedItemNS throws DOMException with INUSE_ATTRIBUTE_ERR code.",
        method = "setNamedItemNS",
        args = {org.w3c.dom.Node.class}
    )
    public void testSetNamedItemNS8() throws Throwable {
        Document doc;
        NamedNodeMap attributes;
        NodeList elementList;
        Element element;
        Attr attr;
        doc = (Document) load("staffNS", builder);
        elementList = doc.getElementsByTagNameNS("*", "address");
        element = (Element) elementList.item(0);
        attributes = element.getAttributes();
        attr = (Attr) attributes.getNamedItemNS("http:
                "domestic");
        element = (Element) elementList.item(1);
        attributes = element.getAttributes();
        {
            boolean success = false;
            try {
                attributes.setNamedItemNS(attr);
            } catch (DOMException ex) {
                success = (ex.code == DOMException.INUSE_ATTRIBUTE_ERR);
            }
            assertTrue("namednodemapsetnameditemns08", success);
        }
    }
}
