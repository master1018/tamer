public final class documentgetdoctype extends DOMTestCase {
   public documentgetdoctype(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      DocumentType docType;
      String docTypeName;
      String nodeValue;
      doc = (Document) load("staff", false);
      docType = doc.getDoctype();
      assertNotNull("docTypeNotNull", docType);
      docTypeName = docType.getName();
      if (("image/svg+xml".equals(getContentType()))) {
          assertEquals("doctypeNameSVG", "svg", docTypeName);
      } else {
          assertEquals("doctypeName", "staff", docTypeName);
      }
    nodeValue = docType.getNodeValue();
      assertNull("initiallyNull", nodeValue);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(documentgetdoctype.class, args);
   }
}
