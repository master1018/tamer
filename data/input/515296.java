public final class hc_nodeelementnodename extends DOMTestCase {
   public hc_nodeelementnodename(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      Element elementNode;
      String elementName;
      doc = (Document) load("hc_staff", false);
      elementNode = doc.getDocumentElement();
      elementName = elementNode.getNodeName();
      if (("image/svg+xml".equals(getContentType()))) {
          assertEquals("svgNodeName", "svg", elementName);
      } else {
          assertEqualsAutoCase("element", "nodeName", "html", elementName);
        }
    }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_nodeelementnodename.class, args);
   }
}
