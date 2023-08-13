@TestTargetClass(DocumentType.class) 
public final class DocumentTypeSystemId extends DOMTestCase {
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
        method = "getSystemId",
        args = {}
    )
   public void testGetSystemId() throws Throwable {
      Document doc;
      DocumentType docType;
      DOMImplementation domImpl;
      String publicId;
      String systemId;
      doc = (Document) load("staffNS", builder);
      domImpl = doc.getImplementation();
      docType = domImpl.createDocumentType("l2:root", "PUB", "SYS");
      publicId = docType.getPublicId();
      systemId = docType.getSystemId();
      assertEquals("documenttypepublicid01", "PUB", publicId);
      assertEquals("documenttypesystemid01", "SYS", systemId);
      }
}
