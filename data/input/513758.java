@TestTargetClass(Element.class) 
public final class ElementGetAttributeNodeNS extends DOMTestCase {
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
        method = "getAttributeNodeNS",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testGetAttributeNodeNS1() throws Throwable {
        Document doc;
        Element element;
        Attr attribute1;
        Attr attribute2;
        Attr attribute;
        String attrValue;
        String attrName;
        String attNodeName;
        String attrLocalName;
        String attrNS;
        doc = (Document) load("staffNS", builder);
        element = doc.createElementNS("namespaceURI", "root");
        attribute1 = doc.createAttributeNS("http:
                "l2:att");
        element.setAttributeNodeNS(attribute1);
        attribute2 = doc.createAttributeNS("http:
                "att");
        element.setAttributeNodeNS(attribute2);
        attribute = element.getAttributeNodeNS("http:
                "att");
        attrValue = attribute.getNodeValue();
        attrName = attribute.getName();
        attNodeName = attribute.getNodeName();
        attrLocalName = attribute.getLocalName();
        attrNS = attribute.getNamespaceURI();
        assertEquals("elementgetattributenodens01_attrValue", "", attrValue);
        assertEquals("elementgetattributenodens01_attrName", "l2:att", attrName);
        assertEquals("elementgetattributenodens01_attrNodeName", "l2:att",
                attNodeName);
        assertEquals("elementgetattributenodens01_attrLocalName", "att",
                attrLocalName);
        assertEquals("elementgetattributenodens01_attrNs",
                "http:
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Doesn't verify DOMException.",
        method = "getAttributeNodeNS",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testGetAttributeNodeNS2() throws Throwable {
        Document doc;
        Element element;
        Attr attribute;
        String attrValue;
        doc = (Document) load("staffNS", builder);
        element = doc.createElementNS("namespaceURI", "root");
        attribute = doc.createAttributeNS("http:
                "l2:att");
        element.setAttributeNodeNS(attribute);
        attribute = element.getAttributeNodeNS("http:
                "att");
        attrValue = attribute.getNodeValue();
        assertEquals("elementgetattributenodens02", "", attrValue);
    }
}
