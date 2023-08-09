public final class hc_nodeappendchilddocfragment extends DOMTestCase {
   public hc_nodeappendchilddocfragment(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node employeeNode;
      NodeList childList;
      DocumentFragment newdocFragment;
      Node newChild1;
      Node newChild2;
      Node child;
      String childName;
      java.util.List result = new java.util.ArrayList();
      Node appendedChild;
      int nodeType;
      java.util.List expected = new java.util.ArrayList();
      expected.add("em");
      expected.add("strong");
      expected.add("code");
      expected.add("sup");
      expected.add("var");
      expected.add("acronym");
      expected.add("br");
      expected.add("b");
      doc = (Document) load("hc_staff", true);
      elementList = doc.getElementsByTagName("p");
      employeeNode = elementList.item(1);
      childList = employeeNode.getChildNodes();
      newdocFragment = doc.createDocumentFragment();
      newChild1 = doc.createElement("br");
      newChild2 = doc.createElement("b");
      appendedChild = newdocFragment.appendChild(newChild1);
      appendedChild = newdocFragment.appendChild(newChild2);
      appendedChild = employeeNode.appendChild(newdocFragment);
      for (int indexN100A2 = 0; indexN100A2 < childList.getLength(); indexN100A2++) {
          child = (Node) childList.item(indexN100A2);
    nodeType = (int) child.getNodeType();
      if (equals(1, nodeType)) {
          childName = child.getNodeName();
      result.add(childName);
      }
      }
      assertEqualsAutoCase("element", "nodeNames", expected, result);
        }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_nodeappendchilddocfragment.class, args);
   }
}
