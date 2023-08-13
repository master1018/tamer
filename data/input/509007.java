public final class documentgetelementsbytagnameNS05 extends DOMTestCase {
   public documentgetelementsbytagnameNS05(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList childList;
      doc = (Document) load("staffNS", false);
      childList = doc.getElementsByTagNameNS("null", "elementId");
      assertSize("documentgetelementsbytagnameNS05", 0, childList);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(documentgetelementsbytagnameNS05.class, args);
   }
}
