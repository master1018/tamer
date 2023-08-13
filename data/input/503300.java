public final class domimplementationfeaturexml extends DOMTestCase {
   public domimplementationfeaturexml(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      state = domImpl.hasFeature("xml", "1.0");
assertTrue("hasXML1", state);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(domimplementationfeaturexml.class, args);
   }
}
