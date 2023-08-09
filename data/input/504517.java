public final class hc_nodeinsertbeforenewchildexists extends DOMTestCase {
   public hc_nodeinsertbeforenewchildexists(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      java.util.List expected = new java.util.ArrayList();
      expected.add("strong");
      expected.add("code");
      expected.add("sup");
      expected.add("var");
      expected.add("em");
      expected.add("acronym");
      java.util.List result = new java.util.ArrayList();
      int nodeType;
      doc = (Document) load("hc_staff", true);
      elementList = doc.getElementsByTagName("p");
      employeeNode = elementList.item(1);
      childList = ((Element) employeeNode).getElementsByTagName("*");
      refChild = childList.item(5);
      newChild = childList.item(0);
      insertedNode = employeeNode.insertBefore(newChild, refChild);
      for (int indexN1008C = 0; indexN1008C < childList.getLength(); indexN1008C++) {
          child = (Node) childList.item(indexN1008C);
    nodeType = (int) child.getNodeType();
      if (equals(1, nodeType)) {
          childName = child.getNodeName();
      result.add(childName);
      }
      }
      assertEqualsAutoCase("element", "childNames", expected, result);
        }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_nodeinsertbeforenewchildexists.class, args);
   }
}
