public final class nodeissupported03 extends DOMTestCase {
   public nodeissupported03(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      DocumentType docType;
      boolean success;
      doc = (Document) load("staffNS", false);
      docType = doc.getDoctype();
      success = docType.isSupported("", "");
      assertFalse("nodeissupported03", success);
}
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodeissupported03.class, args);
   }
}
