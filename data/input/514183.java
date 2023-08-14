public final class hc_nodegetownerdocumentnull extends DOMTestCase {
   public hc_nodegetownerdocumentnull(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      Document ownerDocument;
      doc = (Document) load("hc_staff", false);
      ownerDocument = doc.getOwnerDocument();
      assertNull("nodeGetOwnerDocumentNullAssert1", ownerDocument);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_nodegetownerdocumentnull.class, args);
   }
}
