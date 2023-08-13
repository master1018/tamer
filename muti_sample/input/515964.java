public final class nodelistindexnotzero extends DOMTestCase {
   public nodelistindexnotzero(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      int length;
      String childName;
      doc = (Document) load("staff", false);
      elementList = doc.getElementsByTagName("employee");
      employeeNode = elementList.item(2);
      employeeList = employeeNode.getChildNodes();
      length = (int) employeeList.getLength();
      if (equals(6, length)) {
          child = employeeList.item(1);
      } else {
          child = employeeList.item(3);
      }
    childName = child.getNodeName();
      assertEquals("nodeName", "name", childName);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodelistindexnotzero.class, args);
   }
}
