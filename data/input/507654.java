public final class nodeprocessinginstructionnodetype extends DOMTestCase {
   public nodeprocessinginstructionnodetype(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList testList;
      Node piNode;
      int nodeType;
      doc = (Document) load("staff", false);
      testList = doc.getChildNodes();
      piNode = testList.item(0);
      nodeType = (int) piNode.getNodeType();
      assertEquals("nodeProcessingInstructionNodeTypeAssert1", 7, nodeType);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodeprocessinginstructionnodetype.class, args);
   }
}
