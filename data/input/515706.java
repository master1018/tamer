public final class nodegetlastchild extends DOMTestCase {
   public nodegetlastchild(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node employeeNode;
      Node lchildNode;
      String childName;
      doc = (Document) load("staff", false);
      elementList = doc.getElementsByTagName("employee");
      employeeNode = elementList.item(1);
      lchildNode = employeeNode.getLastChild();
      childName = lchildNode.getNodeName();
      if (equals("#text", childName)) {
          lchildNode = lchildNode.getPreviousSibling();
      childName = lchildNode.getNodeName();
      }
    assertEquals("nodeName", "address", childName);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodegetlastchild.class, args);
   }
}
