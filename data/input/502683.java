public final class nodetextnodename extends DOMTestCase {
   public nodetextnodename(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Element testAddr;
      Node textNode;
      String textName;
      doc = (Document) load("staff", false);
      elementList = doc.getElementsByTagName("address");
      testAddr = (Element) elementList.item(0);
      textNode = testAddr.getFirstChild();
      textName = textNode.getNodeName();
      assertEquals("nodeTextNodeNameAssert1", "#text", textName);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodetextnodename.class, args);
   }
}
