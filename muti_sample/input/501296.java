@TestTargetClass(Element.class) 
public final class ElementSetAttributeNS extends DOMTestCase {
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
        method = "setAttributeNS",
        args = {java.lang.String.class, java.lang.String.class, java.lang.String.class}
    )
    public void testSetAttributeNS1() throws Throwable {
        Document doc;
        Element element;
        Attr attribute;
        String attrName;
        String attrValue;
        doc = (Document) load("staff", builder);
        element = doc.createElementNS("http:
        element.setAttributeNS("http:
                "attr", "value");
        attribute = element.getAttributeNodeNS(
                "http:
        attrName = attribute.getNodeName();
        attrValue = attribute.getNodeValue();
        assertEquals("elementsetattributens01_attrName", "attr", attrName);
        assertEquals("elementsetattributens01_attrValue", "value", attrValue);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify DOMException.",
        method = "setAttributeNS",
        args = {java.lang.String.class, java.lang.String.class, java.lang.String.class}
    )
    public void testSetAttributeNS2() throws Throwable {
        Document doc;
        Element element;
        Attr attribute;
        NodeList elementList;
        String attrName;
        String attrValue;
        doc = (Document) load("staff", builder);
        elementList = doc.getElementsByTagNameNS("*", "address");
        element = (Element) elementList.item(0);
        element.setAttributeNS("http:
                "this:street", "Silver Street");
        attribute = element.getAttributeNodeNS(
                "http:
        attrName = attribute.getNodeName();
        attrValue = attribute.getNodeValue();
        assertEquals("elementsetattributens02_attrName", "this:street",
                attrName);
        assertEquals("elementsetattributens02_attrValue", "Silver Street",
                attrValue);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify DOMException.",
        method = "setAttributeNS",
        args = {java.lang.String.class, java.lang.String.class, java.lang.String.class}
    )
    public void testSetAttributeNS3() throws Throwable {
        Document doc;
        Element element;
        Attr attribute;
        NodeList elementList;
        String attrName;
        String attrValue;
        doc = (Document) load("staffNS", builder);
        elementList = doc.getElementsByTagName("emp:employee");
        element = (Element) elementList.item(0);
        assertNotNull("empEmployeeNotNull", element);
        element.setAttributeNS("http:
                "default1");
        element.setAttributeNS("http:
                "default2");
        attribute = element.getAttributeNodeNS("http:
                "defaultAttr");
        attrName = attribute.getNodeName();
        attrValue = attribute.getNodeValue();
        assertEquals("elementsetattributens03_attrName", "defaultAttr",
                attrName);
        assertEquals("elementsetattributens03_attrValue", "default1", attrValue);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies DOMException with INVALID_CHARACTER_ERR.",
        method = "setAttributeNS",
        args = {java.lang.String.class, java.lang.String.class, java.lang.String.class}
    )
    public void testSetAttributeNS4() throws Throwable {
        Document doc;
        Element element;
        String qualifiedName;
        List<String> qualifiedNames = new ArrayList<String>();
        qualifiedNames.add("/");
        qualifiedNames.add("
        qualifiedNames.add("\\");
        qualifiedNames.add(";");
        qualifiedNames.add("&");
        qualifiedNames.add("*");
        qualifiedNames.add("]]");
        qualifiedNames.add(">");
        qualifiedNames.add("<");
        doc = (Document) load("staffNS", builder);
        element = doc.createElementNS("http:
                "dom:elem");
        for (int indexN10058 = 0; indexN10058 < qualifiedNames.size(); indexN10058++) {
            qualifiedName = (String) qualifiedNames.get(indexN10058);
            {
                boolean success = false;
                try {
                    element.setAttributeNS("http:
                            qualifiedName, "test");
                } catch (DOMException ex) {
                    success = (ex.code == DOMException.INVALID_CHARACTER_ERR);
                }
                assertTrue("elementsetattributens04", success);
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies DOMException with NAMESPACE_ERR code.",
        method = "setAttributeNS",
        args = {java.lang.String.class, java.lang.String.class, java.lang.String.class}
    )
    public void testSetAttributeNS5() throws Throwable {
        Document doc;
        Element element;
        String nullNS = null;
        doc = (Document) load("staffNS", builder);
        element = doc.createElementNS("http:
                "dom:elem");
        {
            boolean success = false;
            try {
                element.setAttributeNS(nullNS, "dom:root", "test");
            } catch (DOMException ex) {
                success = (ex.code == DOMException.NAMESPACE_ERR);
            }
            assertTrue("elementsetattributens05", success);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies DOMException with NAMESPACE_ERR code.",
        method = "setAttributeNS",
        args = {java.lang.String.class, java.lang.String.class, java.lang.String.class}
    )
    public void testSetAttributeNS8() throws Throwable {
        Document doc;
        Element element;
        doc = (Document) load("staffNS", builder);
        element = doc.createElementNS("http:
                "dom:elem");
        {
            boolean success = false;
            try {
                element.setAttributeNS("http:
                        "xmlns", "test");
            } catch (DOMException ex) {
                success = (ex.code == DOMException.NAMESPACE_ERR);
            }
            assertTrue("elementsetattributens08_Err1", success);
        }
        {
            boolean success = false;
            try {
                element.setAttributeNS("http:
                        "xmlns:root", "test");
            } catch (DOMException ex) {
                success = (ex.code == DOMException.NAMESPACE_ERR);
            }
            assertTrue("elementsetattributens08_Err2", success);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies DOMException with NAMESPACE_ERR code.",
        method = "setAttributeNS",
        args = {java.lang.String.class, java.lang.String.class, java.lang.String.class}
    )
    public void testSetAttributeNSURINull() throws Throwable {
          String namespaceURI = null;
          String qualifiedName = "emp:qualifiedName";
          Document doc;
          NodeList elementList;
          Node testAddr;
          doc = (Document) load("staff", builder);
          elementList = doc.getElementsByTagName("employee");
          testAddr = elementList.item(0);
          {
             boolean success = false;
             try {
                ((Element) testAddr).setAttributeNS(namespaceURI, qualifiedName, "newValue");
              } catch (DOMException ex) {
                success = (ex.code == DOMException.NAMESPACE_ERR);
             }
             assertTrue("throw_NAMESPACE_ERR", success);
          }
    }
}
