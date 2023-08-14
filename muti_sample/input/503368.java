public final class hc_nodeinsertbefore extends DOMTestCase {
   public hc_nodeinsertbefore(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", true);
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
      Node insertedNode;
      java.util.List actual = new java.util.ArrayList();
      java.util.List expected = new java.util.ArrayList();
      expected.add("em");
      expected.add("strong");
      expected.add("code");
      expected.add("br");
      expected.add("sup");
      expected.add("var");
      expected.add("acronym");
      int nodeType;
      doc = (Document) load("hc_staff", true);
      elementList = doc.getElementsByTagName("sup");
      refChild = elementList.item(2);
      employeeNode = refChild.getParentNode();
      childList = employeeNode.getChildNodes();
      newChild = doc.createElement("br");
      insertedNode = employeeNode.insertBefore(newChild, refChild);
      for (int indexN10091 = 0; indexN10091 < childList.getLength(); indexN10091++) {
          child = (Node) childList.item(indexN10091);
    nodeType = (int) child.getNodeType();
      if (equals(1, nodeType)) {
          childName = child.getNodeName();
      actual.add(childName);
      }
      }
      assertEqualsAutoCase("element", "nodeNames", expected, actual);
        }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_nodeinsertbefore.class, args);
   }
}
