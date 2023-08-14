public final class nodegetfirstchild extends DOMTestCase {
   public nodegetfirstchild(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node employeeNode;
      Node fchildNode;
      String childName;
      doc = (Document) load("staff", false);
      elementList = doc.getElementsByTagName("employee");
      employeeNode = elementList.item(1);
      fchildNode = employeeNode.getFirstChild();
      childName = fchildNode.getNodeName();
      if (equals("#text", childName)) {
          fchildNode = fchildNode.getNextSibling();
      childName = fchildNode.getNodeName();
      }
    assertEquals("nodeName", "employeeId", childName);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodegetfirstchild.class, args);
   }
}
