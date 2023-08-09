public final class documentcreateelementNS02 extends DOMTestCase {
   public documentcreateelementNS02(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      Element element;
      String namespaceURI = null;
      String qualifiedName = "^^";
      doc = (Document) load("staffNS", false);
      {
         boolean success = false;
         try {
            element = doc.createElementNS(namespaceURI, qualifiedName);
          } catch (DOMException ex) {
            success = (ex.code == DOMException.INVALID_CHARACTER_ERR);
         }
         assertTrue("documentcreateelementNS02", success);
      }
}
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(documentcreateelementNS02.class, args);
   }
}
