public final class hc_nodelisttraverselist extends DOMTestCase {
   public hc_nodelisttraverselist(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node employeeNode;
      NodeList employeeList;
      Node child;
      String childName;
      int nodeType;
      java.util.List result = new java.util.ArrayList();
      java.util.List expected = new java.util.ArrayList();
      expected.add("em");
      expected.add("strong");
      expected.add("code");
      expected.add("sup");
      expected.add("var");
      expected.add("acronym");
      doc = (Document) load("hc_staff", false);
      elementList = doc.getElementsByTagName("p");
      employeeNode = elementList.item(2);
      employeeList = employeeNode.getChildNodes();
      for (int indexN10073 = 0; indexN10073 < employeeList.getLength(); indexN10073++) {
          child = (Node) employeeList.item(indexN10073);
    nodeType = (int) child.getNodeType();
      childName = child.getNodeName();
      if (equals(1, nodeType)) {
          result.add(childName);
      } else {
          assertEquals("textNodeType", 3, nodeType);
      assertEquals("textNodeName", "#text", childName);
      }
      }
      assertEqualsAutoCase("element", "nodeNames", expected, result);
        }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_nodelisttraverselist.class, args);
   }
}
