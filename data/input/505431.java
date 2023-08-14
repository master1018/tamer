public final class getElementsByTagNameNS08 extends DOMTestCase {
   public getElementsByTagNameNS08(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      Element docElem;
      NodeList newList;
      doc = (Document) load("staffNS", false);
      docElem = doc.getDocumentElement();
      newList = docElem.getElementsByTagNameNS("*", "*");
      assertSize("listSize", 36, newList);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(getElementsByTagNameNS08.class, args);
   }
}
