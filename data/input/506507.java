public final class nodedocumentnodename extends DOMTestCase {
   public nodedocumentnodename(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      String documentName;
      doc = (Document) load("staff", false);
      documentName = doc.getNodeName();
      assertEquals("documentNodeName", "#document", documentName);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodedocumentnodename.class, args);
   }
}
