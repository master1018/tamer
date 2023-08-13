public final class nodeappendchilddocfragment extends DOMTestCase {
   public nodeappendchilddocfragment(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
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
      int nodeType;
      Node appendedChild;
      java.util.List expected = new java.util.ArrayList();
      expected.add("employeeId");
      expected.add("name");
      expected.add("position");
      expected.add("salary");
      expected.add("gender");
      expected.add("address");
      expected.add("newChild1");
      expected.add("newChild2");
      doc = (Document) load("staff", true);
      elementList = doc.getElementsByTagName("employee");
      employeeNode = elementList.item(1);
      childList = employeeNode.getChildNodes();
      newdocFragment = doc.createDocumentFragment();
      newChild1 = doc.createElement("newChild1");
      newChild2 = doc.createElement("newChild2");
      appendedChild = newdocFragment.appendChild(newChild1);
      appendedChild = newdocFragment.appendChild(newChild2);
      appendedChild = employeeNode.appendChild(newdocFragment);
      for (int indexN1009F = 0; indexN1009F < childList.getLength(); indexN1009F++) {
          child = (Node) childList.item(indexN1009F);
    nodeType = (int) child.getNodeType();
      if (equals(1, nodeType)) {
          childName = child.getNodeName();
      result.add(childName);
      }
      }
      assertEquals("elementNames", expected, result);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodeappendchilddocfragment.class, args);
   }
}
