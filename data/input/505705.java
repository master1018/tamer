public final class systemId01 extends DOMTestCase {
   public systemId01(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      DocumentType docType;
      String systemId;
      int index;
      doc = (Document) load("staffNS", false);
      docType = doc.getDoctype();
      systemId = docType.getSystemId();
      assertURIEquals("systemId", null, null, null, "staffNS.dtd", null, null, null, null, systemId);
}
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(systemId01.class, args);
   }
}
