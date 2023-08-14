@TestTargetClass(Node.class) 
public final class NodeHasAttributes extends DOMTestCase {
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
        level = TestLevel.COMPLETE,
        notes = "",
        method = "hasAttributes",
        args = {}
    )
    public void testHasAttributes1() throws Throwable {
        Document doc;
        Element element;
        NodeList elementList;
        boolean hasAttributes;
        doc = (Document) load("staff", builder);
        elementList = doc.getElementsByTagName("employee");
        element = (Element) elementList.item(0);
        hasAttributes = element.hasAttributes();
        assertFalse("nodehasattributes01_1", hasAttributes);
        elementList = doc.getElementsByTagName("address");
        element = (Element) elementList.item(0);
        hasAttributes = element.hasAttributes();
        assertTrue("nodehasattributes01_2", hasAttributes);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Verifies that hasAttributes method returns false value.",
        method = "hasAttributes",
        args = {}
    )
    public void testHasAttributes2() throws Throwable {
        Document doc;
        DocumentType docType;
        boolean hasAttributes;
        doc = (Document) load("staffNS", builder);
        docType = doc.getDoctype();
        hasAttributes = docType.hasAttributes();
        assertFalse("nodehasattributes02", hasAttributes);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Verifies that hasAttributes method returns true value.",
        method = "hasAttributes",
        args = {}
    )
    public void testHasAttributes3() throws Throwable {
        Document doc;
        Element element;
        NodeList elementList;
        boolean hasAttributes;
        doc = (Document) load("staffNS", builder);
        elementList = doc.getElementsByTagName("emp:employee");
        element = (Element) elementList.item(0);
        assertNotNull("empEmployeeNotNull", element);
        hasAttributes = element.hasAttributes();
        assertTrue("hasAttributes", hasAttributes);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Verifies that hasAttributes method returns true value.",
        method = "hasAttributes",
        args = {}
    )
    public void testHasAttributes4() throws Throwable {
        Document doc;
        Document newDoc;
        DocumentType docType = null;
        DOMImplementation domImpl;
        Element element;
        Element elementTest;
        Element elementDoc;
        Attr attribute;
        NodeList elementList;
        boolean hasAttributes;
        doc = (Document) load("staffNS", builder);
        domImpl = doc.getImplementation();
        newDoc = domImpl.createDocument("http:
                docType);
        element = newDoc.createElementNS("http:
                "dom:elem");
        attribute = newDoc.createAttribute("attr");
        element.setAttributeNode(attribute);
        elementDoc = newDoc.getDocumentElement();
        elementDoc.appendChild(element);
        elementList = newDoc.getElementsByTagNameNS(
                "http:
        elementTest = (Element) elementList.item(0);
        hasAttributes = elementTest.hasAttributes();
        assertTrue("nodehasattributes04", hasAttributes);
    }
}
