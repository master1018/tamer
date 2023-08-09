public final class nodetextnodevalue extends DOMTestCase {
   public nodetextnodevalue(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Element testAddr;
      Node textNode;
      String textValue;
      doc = (Document) load("staff", false);
      elementList = doc.getElementsByTagName("address");
      testAddr = (Element) elementList.item(0);
      textNode = testAddr.getFirstChild();
      textValue = textNode.getNodeValue();
      assertEquals("nodeTextNodeValueAssert1", "1230 North Ave. Dallas, Texas 98551", textValue);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodetextnodevalue.class, args);
   }
}
