public final class ownerDocument01 extends DOMTestCase {
   public ownerDocument01(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      DocumentType ownerDocument;
      doc = (Document) load("staff", false);
      ownerDocument = (DocumentType) doc.getOwnerDocument();
      assertNull("throw_Null", ownerDocument);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(ownerDocument01.class, args);
   }
}
