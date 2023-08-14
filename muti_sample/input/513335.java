public final class hc_nodelistreturnlastitem extends DOMTestCase {
   public hc_nodelistreturnlastitem(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      int index;
      doc = (Document) load("hc_staff", false);
      elementList = doc.getElementsByTagName("p");
      employeeNode = elementList.item(2);
      employeeList = employeeNode.getChildNodes();
      index = (int) employeeList.getLength();
      index -= 1;
      child = employeeList.item(((int) index));
      childName = child.getNodeName();
      if (equals(12, index)) {
          assertEquals("lastNodeName_w_whitespace", "#text", childName);
      } else {
          assertEqualsAutoCase("element", "lastNodeName", "acronym", childName);
        assertEquals("index", 5, index);
      }
    }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_nodelistreturnlastitem.class, args);
   }
}
