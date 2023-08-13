public final class nodeinsertbeforenodename extends DOMTestCase {
   public nodeinsertbeforenodename(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
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
      doc = (Document) load("staff", true);
      elementList = doc.getElementsByTagName("employee");
      employeeNode = elementList.item(1);
      childList = employeeNode.getChildNodes();
      refChild = childList.item(3);
      newChild = doc.createElement("newChild");
      insertedNode = employeeNode.insertBefore(newChild, refChild);
      childName = insertedNode.getNodeName();
      assertEquals("nodeInsertBeforeNodeNameAssert1", "newChild", childName);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodeinsertbeforenodename.class, args);
   }
}
