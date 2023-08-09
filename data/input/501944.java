public final class internalSubset01 extends DOMTestCase {
   public internalSubset01(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff2", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      DocumentType docType;
      String internal;
      doc = (Document) load("staff2", false);
      docType = doc.getDoctype();
      internal = docType.getInternalSubset();
      assertNull("internalSubsetNull", internal);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(internalSubset01.class, args);
   }
}
