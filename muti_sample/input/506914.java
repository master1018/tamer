public final class domimplementationfeaturexmlversion2 extends DOMTestCase {
   public domimplementationfeaturexmlversion2(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      state = domImpl.hasFeature("xml", "2.0");
assertTrue("domimplementationFeaturexmlVersion2Assert", state);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(domimplementationfeaturexmlversion2.class, args);
   }
}
