@TestTargetClass(Document.class) 
public final class DocumentGeteEementById extends DOMTestCase {
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
        level = TestLevel.PARTIAL,
        notes = "Doesn't verify getElementById method for existent element.",
        method = "getElementById",
        args = {java.lang.String.class}
    )
    public void testGetElementById() throws Throwable {
        Document doc;
        Element element;
        String elementId = "---";
        doc = (Document) load("staffNS", builder);
        element = doc.getElementById(elementId);
        assertNull("documentgetelementbyid01", element);
    }
}
