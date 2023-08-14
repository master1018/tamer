public final class hc_nodetextnodetype extends DOMTestCase {
   public hc_nodetextnodetype(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Element testAddr;
      Node textNode;
      int nodeType;
      doc = (Document) load("hc_staff", false);
      elementList = doc.getElementsByTagName("acronym");
      testAddr = (Element) elementList.item(0);
      textNode = testAddr.getFirstChild();
      nodeType = (int) textNode.getNodeType();
      assertEquals("nodeTextNodeTypeAssert1", 3, nodeType);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_nodetextnodetype.class, args);
   }
}
