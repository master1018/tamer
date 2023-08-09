public final class elementgetelementsbytagnamenomatch extends DOMTestCase {
   public elementgetelementsbytagnamenomatch(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      doc = (Document) load("staff", false);
      elementList = doc.getElementsByTagName("noMatch");
      assertSize("elementGetElementsByTagNameNoMatchNoMatchAssert", 0, elementList);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(elementgetelementsbytagnamenomatch.class, args);
   }
}
