public final class domimplementationcreatedocument07 extends DOMTestCase {
   public domimplementationcreatedocument07(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      DOMImplementation domImpl;
      Document newDoc;
      String namespaceURI = "http:
      DocumentType docType = null;
      doc = (Document) load("staffNS", false);
      domImpl = doc.getImplementation();
      {
         boolean success = false;
         try {
            newDoc = domImpl.createDocument(namespaceURI, ":", docType);
          } catch (DOMException ex) {
            success = (ex.code == DOMException.NAMESPACE_ERR);
         }
         assertTrue("domimplementationcreatedocument07", success);
      }
}
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(domimplementationcreatedocument07.class, args);
   }
}
