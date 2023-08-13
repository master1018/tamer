public final class nodegetprevioussibling extends DOMTestCase {
   public nodegetprevioussibling(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node nameNode;
      Node psNode;
      String psName;
      doc = (Document) load("staff", false);
      elementList = doc.getElementsByTagName("name");
      nameNode = elementList.item(1);
      psNode = nameNode.getPreviousSibling();
      psName = psNode.getNodeName();
      if (equals("#text", psName)) {
          psNode = psNode.getPreviousSibling();
      psName = psNode.getNodeName();
      }
    assertEquals("nodeName", "employeeId", psName);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodegetprevioussibling.class, args);
   }
}
