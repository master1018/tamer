public final class hc_nodechildnodes extends DOMTestCase {
   public hc_nodechildnodes(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node employeeNode;
      Node childNode;
      NodeList childNodes;
      int nodeType;
      String childName;
      java.util.List actual = new java.util.ArrayList();
      java.util.List expected = new java.util.ArrayList();
      expected.add("em");
      expected.add("strong");
      expected.add("code");
      expected.add("sup");
      expected.add("var");
      expected.add("acronym");
      doc = (Document) load("hc_staff", false);
      elementList = doc.getElementsByTagName("p");
      employeeNode = elementList.item(1);
      childNodes = employeeNode.getChildNodes();
      for (int indexN1006C = 0; indexN1006C < childNodes.getLength(); indexN1006C++) {
          childNode = (Node) childNodes.item(indexN1006C);
    nodeType = (int) childNode.getNodeType();
      childName = childNode.getNodeName();
      if (equals(1, nodeType)) {
          actual.add(childName);
      } else {
          assertEquals("textNodeType", 3, nodeType);
      }
      }
      assertEqualsAutoCase("element", "elementNames", expected, actual);
        }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_nodechildnodes.class, args);
   }
}
