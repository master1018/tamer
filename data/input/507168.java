public final class importNode17 extends DOMTestCase {
   public importNode17(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", true);
    preload(contentType, "staffNS", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      Document anotherDoc;
      Node node;
      doc = (Document) load("staffNS", true);
      anotherDoc = (Document) load("staffNS", true);
      {
         boolean success = false;
         try {
            node = doc.importNode(anotherDoc, false);
          } catch (DOMException ex) {
            success = (ex.code == DOMException.NOT_SUPPORTED_ERR);
         }
         assertTrue("throw_NOT_SUPPORTED_ERR", success);
      }
}
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(importNode17.class, args);
   }
}
