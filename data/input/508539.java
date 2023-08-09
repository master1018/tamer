public final class nodeappendchild extends DOMTestCase {
   public nodeappendchild(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node employeeNode;
      NodeList childList;
      Node createdNode;
      Node lchild;
      String childName;
      Node appendedChild;
      doc = (Document) load("staff", true);
      elementList = doc.getElementsByTagName("employee");
      employeeNode = elementList.item(1);
      childList = employeeNode.getChildNodes();
      createdNode = doc.createElement("newChild");
      appendedChild = employeeNode.appendChild(createdNode);
      lchild = employeeNode.getLastChild();
      childName = lchild.getNodeName();
      assertEquals("nodeAppendChildAssert1", "newChild", childName);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodeappendchild.class, args);
   }
}
