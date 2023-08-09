public final class documentcreateelementNS05 extends DOMTestCase {
   public documentcreateelementNS05(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      Element element;
      String namespaceURI = null;
      String qualifiedName = "null:xml";
      doc = (Document) load("staffNS", false);
      {
         boolean success = false;
         try {
            element = doc.createElementNS(namespaceURI, qualifiedName);
          } catch (DOMException ex) {
            success = (ex.code == DOMException.NAMESPACE_ERR);
         }
         assertTrue("documentcreateelementNS05", success);
      }
}
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(documentcreateelementNS05.class, args);
   }
}
