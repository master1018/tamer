public final class documentgetelementsbytagnameNS03 extends DOMTestCase {
   public documentgetelementsbytagnameNS03(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList childList;
      doc = (Document) load("staffNS", false);
      childList = doc.getElementsByTagNameNS("**", "*");
      assertSize("documentgetelementsbytagnameNS03", 0, childList);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(documentgetelementsbytagnameNS03.class, args);
   }
}
