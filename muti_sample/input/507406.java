public final class documentgetelementbyid01 extends DOMTestCase {
   public documentgetelementbyid01(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      Element element;
      String elementId = "---";
      doc = (Document) load("staffNS", false);
      element = doc.getElementById(elementId);
      assertNull("documentgetelementbyid01", element);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(documentgetelementbyid01.class, args);
   }
}
