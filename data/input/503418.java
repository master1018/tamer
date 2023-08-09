public final class createDocument08 extends DOMTestCase {
   public createDocument08(final DOMTestDocumentBuilderFactory factory)  {
      super(factory);
    String contentType = getContentType();
    }
   public void runTest() throws Throwable {
      String namespaceURI = "http:
      DocumentType docType = null;
      DOMImplementation domImpl;
      Document aNewDoc;
      String charact;
      domImpl = getImplementation();
      {
         boolean success = false;
         try {
            aNewDoc = domImpl.createDocument(namespaceURI, "", docType);
          } catch (DOMException ex) {
            success = (ex.code == DOMException.INVALID_CHARACTER_ERR);
         }
         assertTrue("throw_INVALID_CHARACTER_ERR", success);
      }
}
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(createDocument08.class, args);
   }
}
