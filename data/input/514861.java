public final class nodesetprefix09 extends DOMTestCase {
   public nodesetprefix09(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      String value = "#$%&'()@";
      Element element;
      doc = (Document) load("staffNS", true);
      element = doc.createElementNS("http:
      {
         boolean success = false;
         try {
            element.setPrefix(value);
          } catch (DOMException ex) {
            success = (ex.code == DOMException.INVALID_CHARACTER_ERR);
         }
         assertTrue("throw_INVALID_CHARACTER_ERR", success);
      }
}
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodesetprefix09.class, args);
   }
}
