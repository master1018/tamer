public final class hc_nodecommentnodename extends DOMTestCase {
   public hc_nodecommentnodename(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node commentNode;
      int nodeType;
      String commentName;
      String commentNodeName;
      doc = (Document) load("hc_staff", false);
      elementList = doc.getChildNodes();
      for (int indexN10044 = 0; indexN10044 < elementList.getLength(); indexN10044++) {
          commentNode = (Node) elementList.item(indexN10044);
    nodeType = (int) commentNode.getNodeType();
      if (equals(8, nodeType)) {
          commentNodeName = commentNode.getNodeName();
      assertEquals("existingNodeName", "#comment", commentNodeName);
      }
      }
      commentNode = doc.createComment("This is a comment");
      commentNodeName = commentNode.getNodeName();
      assertEquals("createdNodeName", "#comment", commentNodeName);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_nodecommentnodename.class, args);
   }
}
