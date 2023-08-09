public final class hc_nodereplacechild extends DOMTestCase {
   public hc_nodereplacechild(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", true);
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
      doc = (Document) load("hc_staff", true);
      elementList = doc.getElementsByTagName("p");
      employeeNode = elementList.item(1);
      childList = employeeNode.getChildNodes();
      oldChild = childList.item(0);
      newChild = doc.createElement("br");
      replacedNode = employeeNode.replaceChild(newChild, oldChild);
      child = childList.item(0);
      childName = child.getNodeName();
      assertEqualsAutoCase("element", "nodeName", "br", childName);
        }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_nodereplacechild.class, args);
   }
}
