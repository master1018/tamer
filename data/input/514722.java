public final class createDocument06 extends DOMTestCase {
   public createDocument06(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", false);
    }
   public void runTest() throws Throwable {
      String namespaceURI = "http:
      String qualifiedName = "xml:local";
      Document doc;
      DocumentType docType = null;
      DOMImplementation domImpl;
      Document aNewDoc;
      doc = (Document) load("staffNS", false);
      domImpl = doc.getImplementation();
      {
         boolean success = false;
         try {
            aNewDoc = domImpl.createDocument(namespaceURI, qualifiedName, docType);
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
        DOMTestCase.doMain(createDocument06.class, args);
   }
}
