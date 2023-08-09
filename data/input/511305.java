public final class documenttypegetdoctype extends DOMTestCase {
   public documenttypegetdoctype(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      DocumentType docType;
      String name;
      doc = (Document) load("staff", false);
      docType = doc.getDoctype();
      assertNotNull("docTypeNotNull", docType);
      name = docType.getName();
      if (("image/svg+xml".equals(getContentType()))) {
          assertEquals("doctypeName", "svg", name);
      } else {
          assertEquals("documenttypeGetDocTypeAssert", "staff", name);
      }
    }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(documenttypegetdoctype.class, args);
   }
}
