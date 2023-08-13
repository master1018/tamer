public final class nodecommentnodename extends DOMTestCase {
   public nodecommentnodename(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node commentNode;
      int nodeType;
      String commentNodeName;
      doc = (Document) load("staff", false);
      elementList = doc.getChildNodes();
      for (int indexN10040 = 0; indexN10040 < elementList.getLength(); indexN10040++) {
          commentNode = (Node) elementList.item(indexN10040);
    nodeType = (int) commentNode.getNodeType();
      if (equals(8, nodeType)) {
          commentNodeName = commentNode.getNodeName();
      assertEquals("commentNodeName", "#comment", commentNodeName);
      }
      }
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodecommentnodename.class, args);
   }
}
