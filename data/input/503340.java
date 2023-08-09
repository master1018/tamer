public final class nodeclonenodetrue extends DOMTestCase {
   public nodeclonenodetrue(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node employeeNode;
      NodeList childList;
      Node clonedNode;
      NodeList clonedList;
      Node clonedChild;
      String clonedChildName;
      int length;
      java.util.List result = new java.util.ArrayList();
      java.util.List expectedWhitespace = new java.util.ArrayList();
      expectedWhitespace.add("#text");
      expectedWhitespace.add("employeeId");
      expectedWhitespace.add("#text");
      expectedWhitespace.add("name");
      expectedWhitespace.add("#text");
      expectedWhitespace.add("position");
      expectedWhitespace.add("#text");
      expectedWhitespace.add("salary");
      expectedWhitespace.add("#text");
      expectedWhitespace.add("gender");
      expectedWhitespace.add("#text");
      expectedWhitespace.add("address");
      expectedWhitespace.add("#text");
      java.util.List expectedNoWhitespace = new java.util.ArrayList();
      expectedNoWhitespace.add("employeeId");
      expectedNoWhitespace.add("name");
      expectedNoWhitespace.add("position");
      expectedNoWhitespace.add("salary");
      expectedNoWhitespace.add("gender");
      expectedNoWhitespace.add("address");
      doc = (Document) load("staff", true);
      elementList = doc.getElementsByTagName("employee");
      employeeNode = elementList.item(1);
      childList = employeeNode.getChildNodes();
      length = (int) childList.getLength();
      clonedNode = employeeNode.cloneNode(true);
      clonedList = clonedNode.getChildNodes();
      for (int indexN100AE = 0; indexN100AE < clonedList.getLength(); indexN100AE++) {
          clonedChild = (Node) clonedList.item(indexN100AE);
    clonedChildName = clonedChild.getNodeName();
      result.add(clonedChildName);
        }
      if (equals(6, length)) {
          assertEquals("nowhitespace", expectedNoWhitespace, result);
      } else {
          assertEquals("whitespace", expectedWhitespace, result);
      }
    }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodeclonenodetrue.class, args);
   }
}
