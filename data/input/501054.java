@TestTargetClass(Element.class) 
public final class ElementSetAttributeNodeNS extends DOMTestCase {
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
        method = "setAttributeNodeNS",
        args = {org.w3c.dom.Attr.class}
    )
    public void testSetAttributeNodeNS1() throws Throwable {
        Document doc;
        Element element;
        Attr attribute1;
        Attr attribute2;
        Attr attrNode;
        String attrName;
        String attrNS;
        NamedNodeMap attributes;
        int length;
        doc = (Document) load("staff", builder);
        element = doc.createElementNS("http:
                "new:element");
        attribute1 = doc.createAttributeNS("http:
                "p1:att");
        attribute2 = doc.createAttributeNS("http:
                "p2:att");
        attribute2.setValue("value2");
        element.setAttributeNodeNS(attribute1);
        element.setAttributeNodeNS(attribute2);
        attrNode = element.getAttributeNodeNS(
                "http:
        attrName = attrNode.getNodeName();
        attrNS = attrNode.getNamespaceURI();
        assertEquals("elementsetattributenodens01_attrName", "p2:att", attrName);
        assertEquals("elementsetattributenodens01_attrNS",
                "http:
        attributes = element.getAttributes();
        length = (int) attributes.getLength();
        assertEquals("length", 1, length);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify DOMException.",
        method = "setAttributeNodeNS",
        args = {org.w3c.dom.Attr.class}
    )
    public void testSetAttributeNodeNS2() throws Throwable {
        Document doc;
        Element element;
        Element element2;
        Attr attribute;
        Attr attributeCloned;
        Attr newAttr;
        NodeList elementList;
        String attrName;
        String attrValue;
        String nullNS = null;
        doc = (Document) load("staffNS", builder);
        elementList = doc.getElementsByTagNameNS("http:
                "address");
        element = (Element) elementList.item(1);
        attribute = element.getAttributeNodeNS(nullNS, "street");
        attributeCloned = (Attr) attribute.cloneNode(true);
        element2 = (Element) elementList.item(2);
        newAttr = element2.setAttributeNodeNS(attributeCloned);
        attrName = newAttr.getNodeName();
        attrValue = newAttr.getNodeValue();
        assertEquals("elementsetattributenodens02_attrName", "street", attrName);
        assertEquals("elementsetattributenodens02_attrValue", "Yes", attrValue);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies DOMException with INUSE_ATTRIBUTE_ERR code.",
        method = "setAttributeNodeNS",
        args = {org.w3c.dom.Attr.class}
    )
    public void testSetAttributeNodeNS3() throws Throwable {
        Document doc;
        Element element1;
        Element element2;
        Attr attribute;
        NodeList elementList;
        String nullNS = null;
        doc = (Document) load("staffNS", builder);
        elementList = doc.getElementsByTagNameNS("http:
                "address");
        element1 = (Element) elementList.item(1);
        attribute = element1.getAttributeNodeNS(nullNS, "street");
        element2 = (Element) elementList.item(2);
        {
            boolean success = false;
            try {
                element2.setAttributeNodeNS(attribute);
            } catch (DOMException ex) {
                success = (ex.code == DOMException.INUSE_ATTRIBUTE_ERR);
            }
            assertTrue("elementsetattributenodens03", success);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies DOMException with INUSE_ATTRIBUTE_ERR code.",
        method = "setAttributeNodeNS",
        args = {org.w3c.dom.Attr.class}
    )
    public void testSetAttributeNodeNS4() throws Throwable {
        Document doc;
        Element element1;
        Element element2;
        Attr attribute;
        doc = (Document) load("staffNS", builder);
        element1 = doc.createElementNS("http:
        element2 = doc.createElementNS("http:
        attribute = doc.createAttributeNS("http:
        element1.setAttributeNodeNS(attribute);
        {
            boolean success = false;
            try {
                element2.setAttributeNodeNS(attribute);
            } catch (DOMException ex) {
                success = (ex.code == DOMException.INUSE_ATTRIBUTE_ERR);
            }
            assertTrue("elementsetattributenodens04", success);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies DOMException with WRONG_DOCUMENT_ERR code.",
        method = "setAttributeNodeNS",
        args = {org.w3c.dom.Attr.class}
    )
    public void testSetAttributeNodeNS5() throws Throwable {
        Document doc;
        Document docAlt;
        Element element;
        Attr attribute;
        doc = (Document) load("staffNS", builder);
        docAlt = (Document) load("staffNS", builder);
        element = doc.createElementNS("http:
        attribute = docAlt.createAttributeNS("http:
                "attr");
        {
            boolean success = false;
            try {
                element.setAttributeNodeNS(attribute);
            } catch (DOMException ex) {
                success = (ex.code == DOMException.WRONG_DOCUMENT_ERR);
            }
            assertTrue("throw_WRONG_DOCUMENT_ERR", success);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies DOMException with NO_MODIFICATION_ALLOWED_ERR code.",
        method = "setAttributeNodeNS",
        args = {org.w3c.dom.Attr.class}
    )
    public void _testSetAttributeNodeNS6() throws Throwable {
        Document doc;
        Element element;
        Attr attribute;
        Attr attribute2;
        EntityReference entRef;
        NodeList elementList;
        doc = (Document) load("staffNS", builder);
        element = doc.createElementNS("http:
        attribute = doc.createAttributeNS("http:
        entRef = doc.createEntityReference("ent4");
        attribute.appendChild(entRef);
        element.setAttributeNodeNS(attribute);
        elementList = entRef.getChildNodes();
        element = (Element) elementList.item(0);
        attribute2 = doc.createAttributeNS("http:
                "attr2");
        {
            boolean success = false;
            try {
                element.setAttributeNodeNS(attribute2);
            } catch (DOMException ex) {
                success = (ex.code == DOMException.NO_MODIFICATION_ALLOWED_ERR);
            }
            assertTrue("elementsetattributenodens06", success);
        }
    }
}
