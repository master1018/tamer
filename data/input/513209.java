public final class domimplementationhasfeature02 extends DOMTestCase {
   public domimplementationhasfeature02(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      DOMImplementation domImpl;
      boolean success;
      doc = (Document) load("staffNS", false);
      domImpl = doc.getImplementation();
      success = domImpl.hasFeature("Blah Blah", "");
assertFalse("domimplementationhasfeature02", success);
}
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(domimplementationhasfeature02.class, args);
   }
}
