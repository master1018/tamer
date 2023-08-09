public final class nodedocumentnodetype extends DOMTestCase {
   public nodedocumentnodetype(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      int nodeType;
      doc = (Document) load("staff", false);
      nodeType = (int) doc.getNodeType();
      assertEquals("nodeDocumentNodeTypeAssert1", 9, nodeType);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodedocumentnodetype.class, args);
   }
}
