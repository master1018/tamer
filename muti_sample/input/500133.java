public final class documentgetrootnode extends DOMTestCase {
   public documentgetrootnode(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      Element root;
      String rootName;
      doc = (Document) load("staff", false);
      root = doc.getDocumentElement();
      rootName = root.getNodeName();
      if (("image/svg+xml".equals(getContentType()))) {
          assertEquals("svgRootNode", "svg", rootName);
      } else {
          assertEquals("documentGetRootNodeAssert", "staff", rootName);
      }
    }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(documentgetrootnode.class, args);
   }
}
