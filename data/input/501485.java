public final class createElementNS01 extends DOMTestCase {
   public createElementNS01(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", false);
    }
   public void runTest() throws Throwable {
      String namespaceURI = "http:
      String malformedName = "prefix::local";
      Document doc;
      Element newElement;
      doc = (Document) load("staffNS", false);
      {
         boolean success = false;
         try {
            newElement = doc.createElementNS(namespaceURI, malformedName);
          } catch (DOMException ex) {
            success = (ex.code == DOMException.NAMESPACE_ERR);
         }
         assertTrue("throw_NAMESPACE_ERR", success);
      }
}
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(createElementNS01.class, args);
   }
}
