public final class nodeinsertbefore extends DOMTestCase {
   public nodeinsertbefore(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      Node child;
      String childName;
      int length;
      Node insertedNode;
      java.util.List actual = new java.util.ArrayList();
      java.util.List expectedWithWhitespace = new java.util.ArrayList();
      expectedWithWhitespace.add("#text");
      expectedWithWhitespace.add("employeeId");
      expectedWithWhitespace.add("#text");
      expectedWithWhitespace.add("name");
      expectedWithWhitespace.add("#text");
      expectedWithWhitespace.add("position");
      expectedWithWhitespace.add("#text");
      expectedWithWhitespace.add("newChild");
      expectedWithWhitespace.add("salary");
      expectedWithWhitespace.add("#text");
      expectedWithWhitespace.add("gender");
      expectedWithWhitespace.add("#text");
      expectedWithWhitespace.add("address");
      expectedWithWhitespace.add("#text");
      java.util.List expectedWithoutWhitespace = new java.util.ArrayList();
      expectedWithoutWhitespace.add("employeeId");
      expectedWithoutWhitespace.add("name");
      expectedWithoutWhitespace.add("position");
      expectedWithoutWhitespace.add("newChild");
      expectedWithoutWhitespace.add("salary");
      expectedWithoutWhitespace.add("gender");
      expectedWithoutWhitespace.add("address");
      java.util.List expected = new java.util.ArrayList();
      doc = (Document) load("staff", true);
      elementList = doc.getElementsByTagName("employee");
      employeeNode = elementList.item(1);
      childList = employeeNode.getChildNodes();
      length = (int) childList.getLength();
      if (equals(6, length)) {
          refChild = childList.item(3);
      expected =  expectedWithoutWhitespace;
      } else {
          refChild = childList.item(7);
      expected =  expectedWithWhitespace;
      }
    newChild = doc.createElement("newChild");
      insertedNode = employeeNode.insertBefore(newChild, refChild);
      for (int indexN100DC = 0; indexN100DC < childList.getLength(); indexN100DC++) {
          child = (Node) childList.item(indexN100DC);
    childName = child.getNodeName();
      actual.add(childName);
        }
      assertEquals("nodeNames", expected, actual);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodeinsertbefore.class, args);
   }
}
