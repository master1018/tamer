public final class domimplementationfeaturenoversion extends DOMTestCase {
   public domimplementationfeaturenoversion(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      state = domImpl.hasFeature("XML", "");
assertTrue("hasXMLEmpty", state);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(domimplementationfeaturenoversion.class, args);
   }
}
