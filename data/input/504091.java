public final class nodeelementnodename extends DOMTestCase {
   public nodeelementnodename(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      Element elementNode;
      String elementName;
      doc = (Document) load("staff", false);
      elementNode = doc.getDocumentElement();
      elementName = elementNode.getNodeName();
      if (("image/svg+xml".equals(getContentType()))) {
          assertEquals("svgNodeName", "svg", elementName);
      } else {
          assertEquals("nodeElementNodeNameAssert1", "staff", elementName);
      }
    }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodeelementnodename.class, args);
   }
}
