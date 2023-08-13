public final class createDocument04 extends DOMTestCase {
   public createDocument04(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", false);
    preload(contentType, "staffNS", false);
    }
   public void runTest() throws Throwable {
      String namespaceURI = "http:
      String qualifiedName = "namespaceURI:x";
      Document doc;
      DocumentType docType;
      DOMImplementation domImpl;
      Document aNewDoc;
      doc = (Document) load("staffNS", false);
      aNewDoc = (Document) load("staffNS", false);
      docType = doc.getDoctype();
      domImpl = aNewDoc.getImplementation();
      {
         boolean success = false;
         try {
            aNewDoc = domImpl.createDocument(namespaceURI, qualifiedName, docType);
          } catch (DOMException ex) {
            success = (ex.code == DOMException.WRONG_DOCUMENT_ERR);
         }
         assertTrue("throw_WRONG_DOCUMENT_ERR", success);
      }
}
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(createDocument04.class, args);
   }
}
