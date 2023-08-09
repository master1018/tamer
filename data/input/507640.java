@TestTargetClass(Element.class) 
public final class ElementGetElementsByTagNameNS extends DOMTestCase {
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
        method = "getElementsByTagNameNS",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testGetElementsByTagNameNS1() throws Throwable {
        Document doc;
        Element element;
        NodeList elementList;
        doc = (Document) load("staffNS", builder);
        element = doc.getDocumentElement();
        elementList = element.getElementsByTagNameNS("**", "*");
        assertEquals("elementgetelementsbytagnamens02", 0, elementList
                .getLength());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Doesn't verify DOMException.",
        method = "getElementsByTagNameNS",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testGetElementsByTagNameNS4() throws Throwable {
        Document doc;
        Element element;
        Element child1;
        Element child2;
        Element child3;
        NodeList elementList;
        String nullNS = null;
        doc = (Document) load("staffNS", builder);
        element = doc.createElementNS("http:
        child1 = doc.createElementNS("http:
                "dom:child");
        child2 = doc.createElementNS(nullNS, "child");
        child3 = doc.createElementNS("http:
                "dom:child");
        element.appendChild(child1);
        element.appendChild(child2);
        element.appendChild(child3);
        elementList = element.getElementsByTagNameNS(nullNS, "child");
        assertEquals("elementgetelementsbytagnamens04_1", 1, elementList
                .getLength());
        elementList = element.getElementsByTagNameNS("*", "child");
        assertEquals("elementgetelementsbytagnamens04_2", 3, elementList
                .getLength());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Doesn't verify DOMException.",
        method = "getElementsByTagNameNS",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testGetElementsByTagNameNS5() throws Throwable {
        Document doc;
        Element element;
        NodeList elementList;
        doc = (Document) load("staffNS", builder);
        element = doc.getDocumentElement();
        elementList = element.getElementsByTagNameNS(
                "http:
        assertEquals("elementgetelementsbytagnamens05", 1, elementList
                .getLength());
    }
}
