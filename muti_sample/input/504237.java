public final class getElementsByTagNameNS12 extends DOMTestCase {
   public getElementsByTagNameNS12(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      Element docElem;
      NodeList elementList;
      doc = (Document) load("staffNS", false);
      docElem = doc.getDocumentElement();
      elementList = docElem.getElementsByTagNameNS("http:
      assertSize("size", 0, elementList);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(getElementsByTagNameNS12.class, args);
   }
}
