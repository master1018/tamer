public final class nodeelementnodetype extends DOMTestCase {
   public nodeelementnodetype(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      Element rootNode;
      int nodeType;
      doc = (Document) load("staff", false);
      rootNode = doc.getDocumentElement();
      nodeType = (int) rootNode.getNodeType();
      assertEquals("nodeElementNodeTypeAssert1", 1, nodeType);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodeelementnodetype.class, args);
   }
}
