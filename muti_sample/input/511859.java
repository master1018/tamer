public final class nodedocumenttypenodename extends DOMTestCase {
   public nodedocumenttypenodename(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      DocumentType docType;
      String documentTypeName;
      doc = (Document) load("staff", false);
      docType = doc.getDoctype();
      assertNotNull("docTypeNotNull", docType);
      documentTypeName = docType.getNodeName();
      if (("image/svg+xml".equals(getContentType()))) {
          assertEquals("doctypeNameSVG", "svg", documentTypeName);
      } else {
          assertEquals("documentName", "staff", documentTypeName);
      }
    }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodedocumenttypenodename.class, args);
   }
}
