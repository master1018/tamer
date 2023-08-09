public final class elementgettagname extends DOMTestCase {
   public elementgettagname(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      Element root;
      String tagname;
      doc = (Document) load("staff", false);
      root = doc.getDocumentElement();
      tagname = root.getTagName();
      if (("image/svg+xml".equals(getContentType()))) {
          assertEquals("svgTagName", "svg", tagname);
      } else {
          assertEquals("elementGetTagNameAssert", "staff", tagname);
      }
    }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(elementgettagname.class, args);
   }
}
