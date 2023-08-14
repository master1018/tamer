public final class nodegetnextsibling extends DOMTestCase {
   public nodegetnextsibling(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node employeeIdNode;
      Node nsNode;
      String nsName;
      doc = (Document) load("staff", false);
      elementList = doc.getElementsByTagName("employeeId");
      employeeIdNode = elementList.item(1);
      nsNode = employeeIdNode.getNextSibling();
      nsName = nsNode.getNodeName();
      if (equals("#text", nsName)) {
          nsNode = nsNode.getNextSibling();
      nsName = nsNode.getNodeName();
      }
    assertEquals("nodeName", "name", nsName);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodegetnextsibling.class, args);
   }
}
