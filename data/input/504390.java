public final class nodechildnodesappendchild extends DOMTestCase {
   public nodechildnodesappendchild(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      int expectedLength;
      int length;
      doc = (Document) load("staff", true);
      elementList = doc.getElementsByTagName("employee");
      employeeNode = elementList.item(1);
      childList = employeeNode.getChildNodes();
      expectedLength = (int) childList.getLength();
      expectedLength += 1;
      createdNode = doc.createElement("text3");
      employeeNode = employeeNode.appendChild(createdNode);
      length = (int) childList.getLength();
      assertEquals("childNodeLength", expectedLength, length);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodechildnodesappendchild.class, args);
   }
}
