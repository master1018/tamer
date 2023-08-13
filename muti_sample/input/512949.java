public final class hc_documentinvalidcharacterexceptioncreateelement extends DOMTestCase {
   public hc_documentinvalidcharacterexceptioncreateelement(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      Element badElement;
      doc = (Document) load("hc_staff", true);
      {
         boolean success = false;
         try {
            badElement = doc.createElement("invalid^Name");
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
        DOMTestCase.doMain(hc_documentinvalidcharacterexceptioncreateelement.class, args);
   }
}
