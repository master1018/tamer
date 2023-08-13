public final class getElementById02 extends DOMTestCase {
   public getElementById02(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      Element element;
      doc = (Document) load("staffNS", false);
      element = doc.getElementById("Cancun");
      assertNull("throw_Null", element);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(getElementById02.class, args);
   }
}
