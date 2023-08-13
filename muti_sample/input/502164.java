public final class hc_nodedocumentfragmentnodetype extends DOMTestCase {
   public hc_nodedocumentfragmentnodetype(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      DocumentFragment documentFragmentNode;
      int nodeType;
      doc = (Document) load("hc_staff", true);
      documentFragmentNode = doc.createDocumentFragment();
      nodeType = (int) documentFragmentNode.getNodeType();
      assertEquals("nodeDocumentFragmentNodeTypeAssert1", 11, nodeType);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_nodedocumentfragmentnodetype.class, args);
   }
}
