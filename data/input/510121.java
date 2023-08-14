public final class nodereplacechild extends DOMTestCase {
   public nodereplacechild(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node employeeNode;
      NodeList childList;
      Node oldChild;
      Node newChild;
      Node child;
      String childName;
      Node replacedNode;
      doc = (Document) load("staff", true);
      elementList = doc.getElementsByTagName("employee");
      employeeNode = elementList.item(1);
      childList = employeeNode.getChildNodes();
      oldChild = childList.item(0);
      newChild = doc.createElement("newChild");
      replacedNode = employeeNode.replaceChild(newChild, oldChild);
      child = childList.item(0);
      childName = child.getNodeName();
      assertEquals("nodeReplaceChildAssert1", "newChild", childName);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodereplacechild.class, args);
   }
}
