public final class hc_elementgettagname extends DOMTestCase {
   public hc_elementgettagname(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      Element root;
      String tagname;
      doc = (Document) load("hc_staff", false);
      root = doc.getDocumentElement();
      tagname = root.getTagName();
      if (("image/svg+xml".equals(getContentType()))) {
          assertEquals("svgTagname", "svg", tagname);
      } else {
          assertEqualsAutoCase("element", "tagname", "html", tagname);
        }
    }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_elementgettagname.class, args);
   }
}
