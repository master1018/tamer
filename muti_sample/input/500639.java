public final class nodehasattributes02 extends DOMTestCase {
   public nodehasattributes02(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      DocumentType docType;
      boolean hasAttributes;
      doc = (Document) load("staffNS", false);
      docType = doc.getDoctype();
      hasAttributes = docType.hasAttributes();
      assertFalse("nodehasattributes02", hasAttributes);
}
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodehasattributes02.class, args);
   }
}
