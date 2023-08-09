public final class isSupported13 extends DOMTestCase {
   public isSupported13(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      Node rootNode;
      boolean state;
      doc = (Document) load("staff", false);
      rootNode = doc.getDocumentElement();
      state = rootNode.isSupported("Core", "");
      assertTrue("Core", state);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(isSupported13.class, args);
   }
}
