public final class hc_elementgetelementsbytagnamenomatch extends DOMTestCase {
   public hc_elementgetelementsbytagnamenomatch(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      doc = (Document) load("hc_staff", false);
      elementList = doc.getElementsByTagName("noMatch");
      assertSize("elementGetElementsByTagNameNoMatchNoMatchAssert", 0, elementList);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_elementgetelementsbytagnamenomatch.class, args);
   }
}
