public final class hasAttributes02 extends DOMTestCase {
   public hasAttributes02(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList addrList;
      Node addrNode;
      boolean state;
      doc = (Document) load("staff", false);
      addrList = doc.getElementsByTagName("address");
      addrNode = addrList.item(0);
      state = addrNode.hasAttributes();
      assertTrue("throw_True", state);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hasAttributes02.class, args);
   }
}
