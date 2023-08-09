public final class createElementNS02 extends DOMTestCase {
   public createElementNS02(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", false);
    }
   public void runTest() throws Throwable {
      String namespaceURI = null;
      String qualifiedName = "prefix:local";
      Document doc;
      Element newElement;
      doc = (Document) load("staffNS", false);
      {
         boolean success = false;
         try {
            newElement = doc.createElementNS(namespaceURI, qualifiedName);
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
        DOMTestCase.doMain(createElementNS02.class, args);
   }
}
