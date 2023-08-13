public final class createDocumentType01 extends DOMTestCase {
   public createDocumentType01(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", false);
    }
   public void runTest() throws Throwable {
      String publicId = "STAFF";
      String systemId = "staff.xml";
      String malformedName = "prefix::local";
      Document doc;
      DOMImplementation domImpl;
      DocumentType newType;
      doc = (Document) load("staffNS", false);
      domImpl = doc.getImplementation();
      {
         boolean success = false;
         try {
            newType = domImpl.createDocumentType(malformedName, publicId, systemId);
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
        DOMTestCase.doMain(createDocumentType01.class, args);
   }
}
