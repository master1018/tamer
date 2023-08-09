public final class nodeclonetruecopytext extends DOMTestCase {
   public nodeclonetruecopytext(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      NodeList childList;
      Node childNode;
      Node clonedNode;
      Node lastChildNode;
      String childValue;
      doc = (Document) load("staff", true);
      elementList = doc.getElementsByTagName("salary");
      childNode = elementList.item(1);
      clonedNode = childNode.cloneNode(true);
      lastChildNode = clonedNode.getLastChild();
      childValue = lastChildNode.getNodeValue();
      assertEquals("nodeCloneTrueCopyTextAssert1", "35,000", childValue);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodeclonetruecopytext.class, args);
   }
}
