public final class hc_nodeelementnodetype extends DOMTestCase {
   public hc_nodeelementnodetype(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      Element rootNode;
      int nodeType;
      doc = (Document) load("hc_staff", false);
      rootNode = doc.getDocumentElement();
      nodeType = (int) rootNode.getNodeType();
      assertEquals("nodeElementNodeTypeAssert1", 1, nodeType);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_nodeelementnodetype.class, args);
   }
}
