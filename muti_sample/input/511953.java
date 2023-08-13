public final class hc_nodeclonetruecopytext extends DOMTestCase {
   public hc_nodeclonetruecopytext(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node childNode;
      Node clonedNode;
      Node lastChildNode;
      String childValue;
      doc = (Document) load("hc_staff", true);
      elementList = doc.getElementsByTagName("sup");
      childNode = elementList.item(1);
      clonedNode = childNode.cloneNode(true);
      lastChildNode = clonedNode.getLastChild();
      childValue = lastChildNode.getNodeValue();
      assertEquals("cloneContainsText", "35,000", childValue);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_nodeclonetruecopytext.class, args);
   }
}
