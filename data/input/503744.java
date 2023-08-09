@TestTargetClass(Element.class) 
public final class HasAttributeNS extends DOMTestCase {
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
        method = "hasAttributeNS",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testHasAttributeNS1() throws Throwable {
        String localName = "nomatch";
        String namespaceURI = "http:
        Document doc;
        NodeList elementList;
        Element testNode;
        boolean state;
        doc = (Document) load("staffNS", builder);
        elementList = doc.getElementsByTagName("address");
        testNode = (Element) elementList.item(0);
        state = testNode.hasAttributeNS(namespaceURI, localName);
        assertFalse("throw_False", state);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Doesn't verify DOMException.",
        method = "hasAttributeNS",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testHasAttributeNS2() throws Throwable {
        String localName = "domestic";
        String namespaceURI = "http:
        Document doc;
        NodeList elementList;
        Element testNode;
        boolean state;
        doc = (Document) load("staffNS", builder);
        elementList = doc.getElementsByTagName("address");
        testNode = (Element) elementList.item(0);
        state = testNode.hasAttributeNS(namespaceURI, localName);
        assertFalse("throw_False", state);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Doesn't verify DOMException.",
        method = "hasAttributeNS",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testHasAttributeNS3() throws Throwable {
        String localName = "blank";
        String namespaceURI = "http:
        Document doc;
        NodeList elementList;
        Element testNode;
        boolean state;
        doc = (Document) load("staffNS", builder);
        elementList = doc.getElementsByTagName("emp:address");
        testNode = (Element) elementList.item(0);
        assertNotNull("empAddrNotNull", testNode);
        state = testNode.hasAttributeNS(namespaceURI, localName);
        assertFalse("throw_False", state);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Doesn't verify DOMException.",
        method = "hasAttributeNS",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testHasAttributeNS5() throws Throwable {
        String localName = "domestic";
        String namespaceURI = "http:
        Document doc;
        NodeList elementList;
        Element testNode;
        boolean state;
        doc = (Document) load("staffNS", builder);
        elementList = doc.getElementsByTagName("address");
        testNode = (Element) elementList.item(0);
        state = testNode.hasAttributeNS(namespaceURI, localName);
        assertTrue("hasAttribute", state);
    }
}
