public final class domimplementationfeaturecore extends DOMTestCase {
   public domimplementationfeaturecore(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      DOMImplementation domImpl;
      boolean state;
      doc = (Document) load("staff", false);
      domImpl = doc.getImplementation();
      state = domImpl.hasFeature("core", "2.0");
assertTrue("domimplementationFeaturecoreAssert", state);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(domimplementationfeaturecore.class, args);
   }
}
