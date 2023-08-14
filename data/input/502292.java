public final class documenttypesystemid01 extends DOMTestCase {
   public documenttypesystemid01(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      DocumentType docType;
      DOMImplementation domImpl;
      String publicId;
      String systemId;
      doc = (Document) load("staffNS", false);
      domImpl = doc.getImplementation();
      docType = domImpl.createDocumentType("l2:root", "PUB", "SYS");
      publicId = docType.getPublicId();
      systemId = docType.getSystemId();
      assertEquals("documenttypepublicid01", "PUB", publicId);
      assertEquals("documenttypesystemid01", "SYS", systemId);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(documenttypesystemid01.class, args);
   }
}
