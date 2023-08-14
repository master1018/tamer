public final class hasAttribute01 extends DOMTestCase {
   public hasAttribute01(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Element testNode;
      boolean state;
      doc = (Document) load("staff", false);
      elementList = doc.getElementsByTagName("address");
      testNode = (Element) elementList.item(4);
      state = testNode.hasAttribute("domestic");
      assertFalse("throw_False", state);
}
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hasAttribute01.class, args);
   }
}
