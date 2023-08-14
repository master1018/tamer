public final class getElementsByTagNameNS01 extends DOMTestCase {
   public getElementsByTagNameNS01(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", false);
    }
   public void runTest() throws Throwable {
      String namespaceURI = "*";
      String localName = "*";
      Document doc;
      NodeList newList;
      doc = (Document) load("staffNS", false);
      newList = doc.getElementsByTagNameNS(namespaceURI, localName);
      assertSize("throw_Size", 37, newList);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(getElementsByTagNameNS01.class, args);
   }
}
