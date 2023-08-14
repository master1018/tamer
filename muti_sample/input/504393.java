public final class nodechildnodes extends DOMTestCase {
   public nodechildnodes(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node employeeNode;
      NodeList childNodes;
      Node childNode;
      int childType;
      String childName;
      java.util.List elementNames = new java.util.ArrayList();
      java.util.List expectedElementNames = new java.util.ArrayList();
      expectedElementNames.add("employeeId");
      expectedElementNames.add("name");
      expectedElementNames.add("position");
      expectedElementNames.add("salary");
      expectedElementNames.add("gender");
      expectedElementNames.add("address");
      doc = (Document) load("staff", false);
      elementList = doc.getElementsByTagName("employee");
      employeeNode = elementList.item(1);
      childNodes = employeeNode.getChildNodes();
      for (int indexN1006C = 0; indexN1006C < childNodes.getLength(); indexN1006C++) {
          childNode = (Node) childNodes.item(indexN1006C);
    childType = (int) childNode.getNodeType();
      if (equals(1, childType)) {
          childName = childNode.getNodeName();
      elementNames.add(childName);
      }
      }
      assertEquals("elementNames", expectedElementNames, elementNames);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodechildnodes.class, args);
   }
}
