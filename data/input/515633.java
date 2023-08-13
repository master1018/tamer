public final class hc_nodeclonenodetrue extends DOMTestCase {
   public hc_nodeclonenodetrue(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node employeeNode;
      Node clonedNode;
      NodeList clonedList;
      Node clonedChild;
      String clonedChildName;
      NodeList origList;
      Node origChild;
      String origChildName;
      java.util.List result = new java.util.ArrayList();
      java.util.List expected = new java.util.ArrayList();
      doc = (Document) load("hc_staff", true);
      elementList = doc.getElementsByTagName("p");
      employeeNode = elementList.item(1);
      origList = employeeNode.getChildNodes();
      for (int indexN10065 = 0; indexN10065 < origList.getLength(); indexN10065++) {
          origChild = (Node) origList.item(indexN10065);
    origChildName = origChild.getNodeName();
      expected.add(origChildName);
        }
      clonedNode = employeeNode.cloneNode(true);
      clonedList = clonedNode.getChildNodes();
      for (int indexN1007B = 0; indexN1007B < clonedList.getLength(); indexN1007B++) {
          clonedChild = (Node) clonedList.item(indexN1007B);
    clonedChildName = clonedChild.getNodeName();
      result.add(clonedChildName);
        }
      assertEquals("clone", expected, result);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_nodeclonenodetrue.class, args);
   }
}
