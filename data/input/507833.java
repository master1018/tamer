public final class getElementsByTagNameNS05 extends DOMTestCase {
   public getElementsByTagNameNS05(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", false);
    }
   public void runTest() throws Throwable {
      String namespaceURI = "http:
      String localName = "nomatch";
      Document doc;
      NodeList elementList;
      doc = (Document) load("staffNS", false);
      elementList = doc.getElementsByTagNameNS(namespaceURI, localName);
      assertSize("throw_Size", 0, elementList);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(getElementsByTagNameNS05.class, args);
   }
}
