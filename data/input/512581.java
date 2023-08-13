@TestTargetClass(Document.class) 
public final class OwnerDocument extends DOMTestCase {
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
        notes = "Dosn't verify that getOwnerDocument can return not null value.",
        method = "getOwnerDocument",
        args = {}
    )
   public void testGetOwnerDocument() throws Throwable {
      Document doc;
      DocumentType ownerDocument;
      doc = (Document) load("staff", builder);
      ownerDocument = (DocumentType) doc.getOwnerDocument();
      assertNull("throw_Null", ownerDocument);
      }
}
