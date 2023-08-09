public final class nodelisttraverselist extends DOMTestCase {
   public nodelisttraverselist(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      java.util.List result = new java.util.ArrayList();
      int length;
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
      doc = (Document) load("staff", false);
      elementList = doc.getElementsByTagName("employee");
      employeeNode = elementList.item(2);
      employeeList = employeeNode.getChildNodes();
      length = (int) employeeList.getLength();
      for (int indexN100A4 = 0; indexN100A4 < employeeList.getLength(); indexN100A4++) {
          child = (Node) employeeList.item(indexN100A4);
    childName = child.getNodeName();
      result.add(childName);
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
        DOMTestCase.doMain(nodelisttraverselist.class, args);
   }
}
