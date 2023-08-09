public final class createDocument01 extends DOMTestCase {
   public createDocument01(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", false);
    }
   public void runTest() throws Throwable {
      String namespaceURI = "http:
      String malformedName = "prefix::local";
      Document doc;
      DocumentType docType = null;
      DOMImplementation domImpl;
      Document aNewDoc;
      doc = (Document) load("staffNS", false);
      domImpl = doc.getImplementation();
      {
         boolean success = false;
         try {
            aNewDoc = domImpl.createDocument(namespaceURI, malformedName, docType);
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
        DOMTestCase.doMain(createDocument01.class, args);
   }
}
