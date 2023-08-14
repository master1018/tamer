public final class nodereplacechildnodename extends DOMTestCase {
   public nodereplacechildnodename(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node employeeNode;
      NodeList childList;
      Node oldChild;
      Node newChild;
      Node replacedNode;
      int length;
      String childName;
      doc = (Document) load("staff", true);
      elementList = doc.getElementsByTagName("employee");
      employeeNode = elementList.item(1);
      childList = employeeNode.getChildNodes();
      length = (int) childList.getLength();
      oldChild = childList.item(1);
      newChild = doc.createElement("newChild");
      replacedNode = employeeNode.replaceChild(newChild, oldChild);
      childName = replacedNode.getNodeName();
      if (equals(6, length)) {
          assertEquals("nowhitespace", "name", childName);
      } else {
          assertEquals("whitespace", "employeeId", childName);
      }
    }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodereplacechildnodename.class, args);
   }
}
