public final class hc_nodeinsertbeforenodename extends DOMTestCase {
   public hc_nodeinsertbeforenodename(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node employeeNode;
      NodeList childList;
      Node refChild;
      Node newChild;
      Node insertedNode;
      String childName;
      doc = (Document) load("hc_staff", true);
      elementList = doc.getElementsByTagName("p");
      employeeNode = elementList.item(1);
      childList = employeeNode.getChildNodes();
      refChild = childList.item(3);
      newChild = doc.createElement("br");
      insertedNode = employeeNode.insertBefore(newChild, refChild);
      childName = insertedNode.getNodeName();
      assertEqualsAutoCase("element", "nodeName", "br", childName);
        }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_nodeinsertbeforenodename.class, args);
   }
}
