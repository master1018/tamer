public final class hc_documentgetelementsbytagnamelength extends DOMTestCase {
   public hc_documentgetelementsbytagnamelength(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList nameList;
      doc = (Document) load("hc_staff", false);
      nameList = doc.getElementsByTagName("strong");
      assertSize("documentGetElementsByTagNameLengthAssert", 5, nameList);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_documentgetelementsbytagnamelength.class, args);
   }
}
