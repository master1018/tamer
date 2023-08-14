public final class documenttypepublicid01 extends DOMTestCase {
   public documenttypepublicid01(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      DocumentType docType;
      DOMImplementation domImpl;
      String publicId;
      String nullNS = null;
      doc = (Document) load("staffNS", false);
      domImpl = doc.getImplementation();
      docType = domImpl.createDocumentType("l2:root", "PUB", nullNS);
      publicId = docType.getPublicId();
      assertEquals("documenttypepublicid01", "PUB", publicId);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(documenttypepublicid01.class, args);
   }
}
