@TestTargetClass(DocumentType.class) 
public final class InternalSubset extends DOMTestCase {
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
        notes = "Doesn't verify that getInternalSubset method returns the internal subset as a string.",
        method = "getInternalSubset",
        args = {}
    )
   public void testGetInternalSubset() throws Throwable {
      Document doc;
      DocumentType docType;
      String internal;
      doc = (Document) load("staff2", builder);
      docType = doc.getDoctype();
      internal = docType.getInternalSubset();
      assertNull("internalSubsetNull", internal);
      }
}
