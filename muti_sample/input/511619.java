public final class domimplementationcreatedocument04 extends DOMTestCase {
   public domimplementationcreatedocument04(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      DOMImplementation domImpl;
      Document newDoc;
      String namespaceURI = null;
      String qualifiedName = "dom:root";
      DocumentType docType = null;
      doc = (Document) load("staffNS", false);
      domImpl = doc.getImplementation();
      {
         boolean success = false;
         try {
            newDoc = domImpl.createDocument(namespaceURI, qualifiedName, docType);
          } catch (DOMException ex) {
            success = (ex.code == DOMException.NAMESPACE_ERR);
         }
         assertTrue("domimplementationcreatedocument04", success);
      }
}
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(domimplementationcreatedocument04.class, args);
   }
}
