public final class hasAttributes01 extends DOMTestCase {
   public hasAttributes01(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      addrList = doc.getElementsByTagName("name");
      addrNode = addrList.item(0);
      state = addrNode.hasAttributes();
      assertFalse("throw_False", state);
}
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hasAttributes01.class, args);
   }
}
