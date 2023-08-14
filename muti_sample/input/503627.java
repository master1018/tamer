public final class documentgetelementsbytagnametotallength extends DOMTestCase {
   public documentgetelementsbytagnametotallength(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList nameList;
      doc = (Document) load("staff", false);
      nameList = doc.getElementsByTagName("*");
      if (("image/svg+xml".equals(getContentType()))) {
          assertSize("elementCountSVG", 39, nameList);
      } else {
          assertSize("documentGetElementsByTagNameTotalLengthAssert", 37, nameList);
      }
    }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(documentgetelementsbytagnametotallength.class, args);
   }
}
