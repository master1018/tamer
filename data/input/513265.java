public final class nodeinsertbeforenewchildexists extends DOMTestCase {
   public nodeinsertbeforenewchildexists(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      int length;
      String childName;
      Node insertedNode;
      java.util.List expectedWhitespace = new java.util.ArrayList();
      expectedWhitespace.add("#text");
      expectedWhitespace.add("#text");
      expectedWhitespace.add("name");
      expectedWhitespace.add("#text");
      expectedWhitespace.add("position");
      expectedWhitespace.add("#text");
      expectedWhitespace.add("salary");
      expectedWhitespace.add("#text");
      expectedWhitespace.add("gender");
      expectedWhitespace.add("#text");
      expectedWhitespace.add("employeeId");
      expectedWhitespace.add("address");
      expectedWhitespace.add("#text");
      java.util.List expectedNoWhitespace = new java.util.ArrayList();
      expectedNoWhitespace.add("name");
      expectedNoWhitespace.add("position");
      expectedNoWhitespace.add("salary");
      expectedNoWhitespace.add("gender");
      expectedNoWhitespace.add("employeeId");
      expectedNoWhitespace.add("address");
      java.util.List expected = new java.util.ArrayList();
      java.util.List result = new java.util.ArrayList();
      doc = (Document) load("staff", true);
      elementList = doc.getElementsByTagName("employee");
      employeeNode = elementList.item(1);
      childList = employeeNode.getChildNodes();
      length = (int) childList.getLength();
      if (equals(6, length)) {
          expected =  expectedNoWhitespace;
      refChild = childList.item(5);
      newChild = childList.item(0);
      } else {
          expected =  expectedWhitespace;
      refChild = childList.item(11);
      newChild = childList.item(1);
      }
    insertedNode = employeeNode.insertBefore(newChild, refChild);
      for (int indexN100DD = 0; indexN100DD < childList.getLength(); indexN100DD++) {
          child = (Node) childList.item(indexN100DD);
    childName = child.getNodeName();
      result.add(childName);
        }
      assertEquals("childNames", expected, result);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodeinsertbeforenewchildexists.class, args);
   }
}
