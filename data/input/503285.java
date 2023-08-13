public final class hc_nodeappendchild extends DOMTestCase {
   public hc_nodeappendchild(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", true);
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
      doc = (Document) load("hc_staff", true);
      elementList = doc.getElementsByTagName("p");
      employeeNode = elementList.item(1);
      childList = employeeNode.getChildNodes();
      createdNode = doc.createElement("br");
      appendedChild = employeeNode.appendChild(createdNode);
      lchild = employeeNode.getLastChild();
      childName = lchild.getNodeName();
      assertEqualsAutoCase("element", "nodeName", "br", childName);
        }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_nodeappendchild.class, args);
   }
}
