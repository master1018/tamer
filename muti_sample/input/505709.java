public final class hc_documentgetdoctype extends DOMTestCase {
   public hc_documentgetdoctype(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      DocumentType docType;
      String docTypeName;
      String nodeValue;
      NamedNodeMap attributes;
      doc = (Document) load("hc_staff", false);
      docType = doc.getDoctype();
      if (
    !(("text/html".equals(getContentType())))
) {
          assertNotNull("docTypeNotNull", docType);
      }
      if ((docType != null)) {
          docTypeName = docType.getName();
      if (("image/svg+xml".equals(getContentType()))) {
          assertEquals("nodeNameSVG", "svg", docTypeName);
      } else {
          assertEquals("nodeName", "html", docTypeName);
      }
    nodeValue = docType.getNodeValue();
      assertNull("nodeValue", nodeValue);
      attributes = docType.getAttributes();
      assertNull("attributes", attributes);
      }
    }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_documentgetdoctype.class, args);
   }
}
