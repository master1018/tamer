public final class nodecommentnodetype extends DOMTestCase {
   public nodecommentnodetype(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList testList;
      Node commentNode;
      String commentNodeName;
      int nodeType;
      doc = (Document) load("staff", false);
      testList = doc.getChildNodes();
      for (int indexN10040 = 0; indexN10040 < testList.getLength(); indexN10040++) {
          commentNode = (Node) testList.item(indexN10040);
    commentNodeName = commentNode.getNodeName();
      if (equals("#comment", commentNodeName)) {
          nodeType = (int) commentNode.getNodeType();
      assertEquals("nodeCommentNodeTypeAssert1", 8, nodeType);
      }
      }
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodecommentnodetype.class, args);
   }
}
