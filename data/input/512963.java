public final class documentgetimplementation extends DOMTestCase {
   public documentgetimplementation(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      DOMImplementation docImpl;
      boolean state;
      doc = (Document) load("staff", false);
      docImpl = doc.getImplementation();
      state = docImpl.hasFeature("XML", "1.0");
assertTrue("documentGetImplementationAssert", state);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(documentgetimplementation.class, args);
   }
}
