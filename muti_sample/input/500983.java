@TestTargetClass(Element.class) 
public final class ElementHasAttributeNS extends DOMTestCase {
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
    public void _testHasAttributeNS1() throws Throwable {
        Document doc;
        Element element;
        boolean state;
        NodeList elementList;
        doc = (Document) load("staffNS", builder);
        elementList = doc.getElementsByTagNameNS("*", "employee");
        element = (Element) elementList.item(0);
        state = element
                .hasAttributeNS("http:
        assertTrue("elementhasattributens01", state);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Doesn't verify DOMException.",
        method = "hasAttributeNS",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testHasAttributeNS2() throws Throwable {
        Document doc;
        Element element;
        boolean state;
        Attr attribute;
        doc = (Document) load("staff", builder);
        element = doc.createElementNS("http:
        attribute = doc.createAttributeNS("http:
        element.setAttributeNode(attribute);
        state = element.hasAttributeNS("http:
        assertTrue("hasDomesticAttr", state);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Doesn't verify DOMException.",
        method = "hasAttributeNS",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testHasAttributeNS3() throws Throwable {
        Document doc;
        Element element;
        boolean state;
        Attr attribute;
        String nullNS = null;
        doc = (Document) load("staff", builder);
        element = doc.createElementNS("http:
        assertNotNull("createElementNotNull", element);
        attribute = doc.createAttributeNS(nullNS, "domestic");
        element.setAttributeNode(attribute);
        state = element.hasAttributeNS(nullNS, "domestic");
        assertTrue("elementhasattributens03", state);
    }
}
