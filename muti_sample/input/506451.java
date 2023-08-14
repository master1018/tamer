public final class hc_nodeinsertbeforerefchildnull extends DOMTestCase {
   public hc_nodeinsertbeforerefchildnull(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", true);
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
      doc = (Document) load("hc_staff", true);
      elementList = doc.getElementsByTagName("p");
      employeeNode = elementList.item(1);
      childList = employeeNode.getChildNodes();
      newChild = doc.createElement("br");
      insertedNode = employeeNode.insertBefore(newChild, refChild);
      child = employeeNode.getLastChild();
      childName = child.getNodeName();
      assertEqualsAutoCase("element", "nodeName", "br", childName);
        }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_nodeinsertbeforerefchildnull.class, args);
   }
}
