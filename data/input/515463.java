public final class documentgetelementsbytagnamelength extends DOMTestCase {
   public documentgetelementsbytagnamelength(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList nameList;
      doc = (Document) load("staff", false);
      nameList = doc.getElementsByTagName("name");
      assertSize("documentGetElementsByTagNameLengthAssert", 5, nameList);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(documentgetelementsbytagnamelength.class, args);
   }
}
