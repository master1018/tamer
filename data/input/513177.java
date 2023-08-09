public final class nodelistreturnfirstitem extends DOMTestCase {
   public nodelistreturnfirstitem(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      child = employeeList.item(0);
      childName = child.getNodeName();
      length = (int) employeeList.getLength();
      if (equals(6, length)) {
          assertEqualsIgnoreCase("firstChildNoWhitespace", "employeeId", childName);
} else {
          assertEqualsIgnoreCase("firstChildWithWhitespace", "#text", childName);
}
    }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodelistreturnfirstitem.class, args);
   }
}
