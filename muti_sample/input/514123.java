public final class hc_nodegetownerdocument extends DOMTestCase {
   public hc_nodegetownerdocument(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node docNode;
      Document ownerDocument;
      Element docElement;
      String elementName;
      doc = (Document) load("hc_staff", false);
      elementList = doc.getElementsByTagName("p");
      docNode = elementList.item(1);
      ownerDocument = docNode.getOwnerDocument();
      docElement = ownerDocument.getDocumentElement();
      elementName = docElement.getNodeName();
      if (("image/svg+xml".equals(getContentType()))) {
          assertEquals("svgNodeName", "svg", elementName);
      } else {
          assertEqualsAutoCase("element", "ownerDocElemTagName", "html", elementName);
        }
    }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_nodegetownerdocument.class, args);
   }
}
