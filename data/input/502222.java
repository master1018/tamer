public final class documenttypeinternalSubset01 extends DOMTestCase {
   public documenttypeinternalSubset01(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      DocumentType docType;
      DOMImplementation domImpl;
      String internal;
      String nullNS = null;
      doc = (Document) load("staffNS", false);
      domImpl = doc.getImplementation();
      docType = domImpl.createDocumentType("l2:root", nullNS, nullNS);
      internal = docType.getInternalSubset();
      assertNull("internalSubsetNull", internal);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(documenttypeinternalSubset01.class, args);
   }
}
