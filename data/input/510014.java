public final class hc_nodedocumentnodetype extends DOMTestCase {
   public hc_nodedocumentnodetype(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      int nodeType;
      doc = (Document) load("hc_staff", false);
      nodeType = (int) doc.getNodeType();
      assertEquals("nodeDocumentNodeTypeAssert1", 9, nodeType);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_nodedocumentnodetype.class, args);
   }
}
