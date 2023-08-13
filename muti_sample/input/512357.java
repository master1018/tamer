public final class hasAttributeNS03 extends DOMTestCase {
   public hasAttributeNS03(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", false);
    }
   public void runTest() throws Throwable {
      String localName = "blank";
      String namespaceURI = "http:
      Document doc;
      NodeList elementList;
      Element testNode;
      boolean state;
      doc = (Document) load("staffNS", false);
      elementList = doc.getElementsByTagName("emp:address");
      testNode = (Element) elementList.item(0);
      assertNotNull("empAddrNotNull", testNode);
      state = testNode.hasAttributeNS(namespaceURI, localName);
      assertFalse("throw_False", state);
}
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hasAttributeNS03.class, args);
   }
}
