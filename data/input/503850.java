public final class nodedocumentnodevalue extends DOMTestCase {
   public nodedocumentnodevalue(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      String documentValue;
      doc = (Document) load("staff", false);
      documentValue = doc.getNodeValue();
      assertNull("documentNodeValueNull", documentValue);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodedocumentnodevalue.class, args);
   }
}
