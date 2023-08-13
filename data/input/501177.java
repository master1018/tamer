public final class documentgetelementsbytagnameNS01 extends DOMTestCase {
   public documentgetelementsbytagnameNS01(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      Document newDoc;
      DocumentType docType = null;
      DOMImplementation domImpl;
      NodeList childList;
      String nullNS = null;
      doc = (Document) load("staffNS", false);
      domImpl = doc.getImplementation();
      newDoc = domImpl.createDocument(nullNS, "root", docType);
      childList = newDoc.getElementsByTagNameNS("*", "*");
      assertSize("documentgetelementsbytagnameNS01", 1, childList);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(documentgetelementsbytagnameNS01.class, args);
   }
}
