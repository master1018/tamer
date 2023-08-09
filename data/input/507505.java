public final class hc_nodechildnodesappendchild extends DOMTestCase {
   public hc_nodechildnodesappendchild(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node employeeNode;
      NodeList childList;
      Node createdNode;
      Node childNode;
      String childName;
      int childType;
      Node textNode;
      java.util.List actual = new java.util.ArrayList();
      java.util.List expected = new java.util.ArrayList();
      expected.add("em");
      expected.add("strong");
      expected.add("code");
      expected.add("sup");
      expected.add("var");
      expected.add("acronym");
      expected.add("br");
      doc = (Document) load("hc_staff", true);
      elementList = doc.getElementsByTagName("p");
      employeeNode = elementList.item(1);
      childList = employeeNode.getChildNodes();
      createdNode = doc.createElement("br");
      employeeNode = employeeNode.appendChild(createdNode);
      for (int indexN10087 = 0; indexN10087 < childList.getLength(); indexN10087++) {
          childNode = (Node) childList.item(indexN10087);
    childName = childNode.getNodeName();
      childType = (int) childNode.getNodeType();
      if (equals(1, childType)) {
          actual.add(childName);
      } else {
          assertEquals("textNodeType", 3, childType);
      }
      }
      assertEqualsAutoCase("element", "childElements", expected, actual);
        }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_nodechildnodesappendchild.class, args);
   }
}
