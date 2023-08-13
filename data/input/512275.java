public final class nodedocumentfragmentnodetype extends DOMTestCase {
   public nodedocumentfragmentnodetype(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      DocumentFragment documentFragmentNode;
      int nodeType;
      doc = (Document) load("staff", true);
      documentFragmentNode = doc.createDocumentFragment();
      nodeType = (int) documentFragmentNode.getNodeType();
      assertEquals("nodeDocumentFragmentNodeTypeAssert1", 11, nodeType);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodedocumentfragmentnodetype.class, args);
   }
}
