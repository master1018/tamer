public final class documentcreateelementNS06 extends DOMTestCase {
   public documentcreateelementNS06(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      Document newDoc;
      DocumentType docType = null;
      DOMImplementation domImpl;
      Element element;
      String namespaceURI = "http:
      String qualifiedName = "xml:root";
      doc = (Document) load("staffNS", false);
      domImpl = doc.getImplementation();
      newDoc = domImpl.createDocument("http:
      {
         boolean success = false;
         try {
            element = newDoc.createElementNS(namespaceURI, qualifiedName);
          } catch (DOMException ex) {
            success = (ex.code == DOMException.NAMESPACE_ERR);
         }
         assertTrue("documentcreateelementNS06", success);
      }
}
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(documentcreateelementNS06.class, args);
   }
}
