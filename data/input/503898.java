@TestTargetClass(DocumentType.class) 
public final class PublicId extends DOMTestCase {
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
        method = "getPublicId",
        args = {}
    )
   public void testGetPublicId() throws Throwable {
      Document doc;
      DocumentType docType;
      String publicId;
      doc = (Document) load("staffNS", builder);
      docType = doc.getDoctype();
      publicId = docType.getPublicId();
      assertEquals("throw_Equals", "STAFF", publicId);
      }
}
