public final class nodegetownerdocumentnull extends DOMTestCase {
   public nodegetownerdocumentnull(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      Document ownerDocument;
      doc = (Document) load("staff", false);
      ownerDocument = doc.getOwnerDocument();
      assertNull("documentOwnerDocumentNull", ownerDocument);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodegetownerdocumentnull.class, args);
   }
}
