public final class nodeinsertbeforerefchildnull extends DOMTestCase {
   public nodeinsertbeforerefchildnull(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node employeeNode;
      NodeList childList;
      Node refChild = null;
      Node newChild;
      Node child;
      String childName;
      Node insertedNode;
      doc = (Document) load("staff", true);
      elementList = doc.getElementsByTagName("employee");
      employeeNode = elementList.item(1);
      childList = employeeNode.getChildNodes();
      newChild = doc.createElement("newChild");
      insertedNode = employeeNode.insertBefore(newChild, refChild);
      child = employeeNode.getLastChild();
      childName = child.getNodeName();
      assertEquals("nodeInsertBeforeRefChildNullAssert1", "newChild", childName);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodeinsertbeforerefchildnull.class, args);
   }
}
