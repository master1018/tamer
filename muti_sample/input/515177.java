public final class nodechildnodesempty extends DOMTestCase {
   public nodechildnodesempty(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node employeeNode;
      NodeList childList;
      Node secondCNode;
      Node textNode;
      NodeList childNodesList;
      doc = (Document) load("staff", false);
      elementList = doc.getElementsByTagName("employee");
      employeeNode = elementList.item(1);
      childList = employeeNode.getChildNodes();
      secondCNode = childList.item(1);
      textNode = secondCNode.getFirstChild();
      childNodesList = textNode.getChildNodes();
      assertSize("nodeChildNodesEmptyAssert1", 0, childNodesList);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodechildnodesempty.class, args);
   }
}
