public final class nodelistreturnlastitem extends DOMTestCase {
   public nodelistreturnlastitem(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node employeeNode;
      NodeList employeeList;
      Node child;
      String childName;
      int length;
      doc = (Document) load("staff", false);
      elementList = doc.getElementsByTagName("employee");
      employeeNode = elementList.item(2);
      employeeList = employeeNode.getChildNodes();
      length = (int) employeeList.getLength();
      if (equals(6, length)) {
          child = employeeList.item(5);
      childName = child.getNodeName();
      assertEquals("nodeName1", "address", childName);
      } else {
          child = employeeList.item(12);
      childName = child.getNodeName();
      assertEquals("nodeName2", "#text", childName);
      }
    }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodelistreturnlastitem.class, args);
   }
}
