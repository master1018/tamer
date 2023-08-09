public final class publicId01 extends DOMTestCase {
   public publicId01(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      DocumentType docType;
      String publicId;
      doc = (Document) load("staffNS", false);
      docType = doc.getDoctype();
      publicId = docType.getPublicId();
      assertEquals("throw_Equals", "STAFF", publicId);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(publicId01.class, args);
   }
}
