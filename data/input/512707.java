public final class documentinvalidcharacterexceptioncreateattribute extends DOMTestCase {
   public documentinvalidcharacterexceptioncreateattribute(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      Attr createdAttr;
      doc = (Document) load("staff", true);
      {
         boolean success = false;
         try {
            createdAttr = doc.createAttribute("invalid^Name");
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
        DOMTestCase.doMain(documentinvalidcharacterexceptioncreateattribute.class, args);
   }
}
