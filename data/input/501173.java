public final class createDocumentType04 extends DOMTestCase {
   public createDocumentType04(final DOMTestDocumentBuilderFactory factory)  {
      super(factory);
    String contentType = getContentType();
    }
   public void runTest() throws Throwable {
      String publicId = "http:
      String systemId = "myDoc.dtd";
      String qualifiedName;
      DocumentType docType = null;
      DOMImplementation domImpl;
      domImpl = getImplementation();
      {
         boolean success = false;
         try {
            docType = domImpl.createDocumentType("", publicId, systemId);
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
        DOMTestCase.doMain(createDocumentType04.class, args);
   }
}
