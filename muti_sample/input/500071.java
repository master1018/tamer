public final class hc_nodedocumentnodevalue extends DOMTestCase {
   public hc_nodedocumentnodevalue(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      String documentValue;
      doc = (Document) load("hc_staff", false);
      documentValue = doc.getNodeValue();
      assertNull("documentNodeValue", documentValue);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_nodedocumentnodevalue.class, args);
   }
}
