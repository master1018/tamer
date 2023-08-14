public final class nodedocumenttypenodetype extends DOMTestCase {
   public nodedocumenttypenodetype(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      DocumentType documentTypeNode;
      int nodeType;
      doc = (Document) load("staff", false);
      documentTypeNode = doc.getDoctype();
      assertNotNull("doctypeNotNull", documentTypeNode);
      nodeType = (int) documentTypeNode.getNodeType();
      assertEquals("nodeType", 10, nodeType);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodedocumenttypenodetype.class, args);
   }
}
