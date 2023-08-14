public final class hc_documentgetrootnode extends DOMTestCase {
   public hc_documentgetrootnode(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      Element root;
      String rootName;
      doc = (Document) load("hc_staff", false);
      root = doc.getDocumentElement();
      rootName = root.getNodeName();
      if (("image/svg+xml".equals(getContentType()))) {
          assertEquals("svgTagName", "svg", rootName);
      } else {
          assertEqualsAutoCase("element", "docElemName", "html", rootName);
        }
    }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_documentgetrootnode.class, args);
   }
}
