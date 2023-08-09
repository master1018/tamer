public final class documentimportnode08 extends DOMTestCase {
   public documentimportnode08(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      org.w3c.domts.DocumentBuilderSetting[] settings = 
          new org.w3c.domts.DocumentBuilderSetting[] {
org.w3c.domts.DocumentBuilderSetting.namespaceAware
        };
        DOMTestDocumentBuilderFactory testFactory = factory.newInstance(settings);
        setFactory(testFactory);
    String contentType = getContentType();
    preload(contentType, "staffNS", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      Node imported;
      DocumentType docType;
      DOMImplementation domImpl;
      String nullNS = null;
      doc = (Document) load("staffNS", true);
      domImpl = doc.getImplementation();
      docType = domImpl.createDocumentType("test:root", nullNS, nullNS);
      {
         boolean success = false;
         try {
            imported = doc.importNode(docType, true);
          } catch (DOMException ex) {
            success = (ex.code == DOMException.NOT_SUPPORTED_ERR);
         }
         assertTrue("throw_NOT_SUPPORTED_ERR", success);
      }
}
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(documentimportnode08.class, args);
   }
}
