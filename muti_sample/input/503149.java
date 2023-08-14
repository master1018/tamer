public final class hc_noderemovechildnode extends DOMTestCase {
   public hc_noderemovechildnode(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      NodeList emList;
      Node employeeNode;
      NodeList childList;
      Node oldChild;
      Node child;
      String childName;
      int length;
      Node removedChild;
      String removedName;
      int nodeType;
      java.util.List expected = new java.util.ArrayList();
      expected.add("strong");
      expected.add("code");
      expected.add("sup");
      expected.add("var");
      expected.add("acronym");
      java.util.List actual = new java.util.ArrayList();
      doc = (Document) load("hc_staff", true);
      elementList = doc.getElementsByTagName("p");
      employeeNode = elementList.item(1);
      childList = employeeNode.getChildNodes();
      emList = ((Element) employeeNode).getElementsByTagName("em");
      oldChild = emList.item(0);
      removedChild = employeeNode.removeChild(oldChild);
      removedName = removedChild.getNodeName();
      assertEqualsAutoCase("element", "removedName", "em", removedName);
        for (int indexN10098 = 0; indexN10098 < childList.getLength(); indexN10098++) {
          child = (Node) childList.item(indexN10098);
    nodeType = (int) child.getNodeType();
      childName = child.getNodeName();
      if (equals(1, nodeType)) {
          actual.add(childName);
      } else {
          assertEquals("textNodeType", 3, nodeType);
      assertEquals("textNodeName", "#text", childName);
      }
      }
      assertEqualsAutoCase("element", "childNames", expected, actual);
        }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_noderemovechildnode.class, args);
   }
}
