public final class nodereplacechildnewchildexists extends DOMTestCase {
   public nodereplacechildnewchildexists(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node employeeNode;
      NodeList childList;
      Node oldChild = null;
      Node newChild = null;
      String childName;
      Node childNode;
      int length;
      java.util.List actual = new java.util.ArrayList();
      java.util.List expected = new java.util.ArrayList();
      java.util.List expectedWithoutWhitespace = new java.util.ArrayList();
      expectedWithoutWhitespace.add("name");
      expectedWithoutWhitespace.add("position");
      expectedWithoutWhitespace.add("salary");
      expectedWithoutWhitespace.add("gender");
      expectedWithoutWhitespace.add("employeeId");
      java.util.List expectedWithWhitespace = new java.util.ArrayList();
      expectedWithWhitespace.add("#text");
      expectedWithWhitespace.add("#text");
      expectedWithWhitespace.add("name");
      expectedWithWhitespace.add("#text");
      expectedWithWhitespace.add("position");
      expectedWithWhitespace.add("#text");
      expectedWithWhitespace.add("salary");
      expectedWithWhitespace.add("#text");
      expectedWithWhitespace.add("gender");
      expectedWithWhitespace.add("#text");
      expectedWithWhitespace.add("employeeId");
      expectedWithWhitespace.add("#text");
      Node replacedChild;
      doc = (Document) load("staff", true);
      elementList = doc.getElementsByTagName("employee");
      employeeNode = elementList.item(1);
      childList = employeeNode.getChildNodes();
      length = (int) childList.getLength();
      if (equals(13, length)) {
          newChild = childList.item(1);
      oldChild = childList.item(11);
      expected =  expectedWithWhitespace;
      } else {
          newChild = childList.item(0);
      oldChild = childList.item(5);
      expected =  expectedWithoutWhitespace;
      }
    replacedChild = employeeNode.replaceChild(newChild, oldChild);
      assertSame("return_value_same", oldChild, replacedChild);
for (int indexN100DE = 0; indexN100DE < childList.getLength(); indexN100DE++) {
          childNode = (Node) childList.item(indexN100DE);
    childName = childNode.getNodeName();
      actual.add(childName);
        }
      assertEquals("childNames", expected, actual);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodereplacechildnewchildexists.class, args);
   }
}
