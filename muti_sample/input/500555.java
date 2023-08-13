public final class hc_nodedocumentnodename extends DOMTestCase {
   public hc_nodedocumentnodename(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      String documentName;
      doc = (Document) load("hc_staff", false);
      documentName = doc.getNodeName();
      assertEquals("documentNodeName", "#document", documentName);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_nodedocumentnodename.class, args);
   }
}
