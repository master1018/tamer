public final class nodegetownerdocument extends DOMTestCase {
   public nodegetownerdocument(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node docNode;
      Document ownerDocument;
      Element docElement;
      String elementName;
      doc = (Document) load("staff", false);
      elementList = doc.getElementsByTagName("employee");
      docNode = elementList.item(1);
      ownerDocument = docNode.getOwnerDocument();
      docElement = ownerDocument.getDocumentElement();
      elementName = docElement.getNodeName();
      if (("image/svg+xml".equals(getContentType()))) {
          assertEquals("svgTagName", "svg", elementName);
      } else {
          assertEquals("nodeGetOwnerDocumentAssert1", "staff", elementName);
      }
    }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodegetownerdocument.class, args);
   }
}
