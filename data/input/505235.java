public final class hc_nodelistindexequalzero extends DOMTestCase {
   public hc_nodelistindexequalzero(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      int length;
      doc = (Document) load("hc_staff", false);
      elementList = doc.getElementsByTagName("p");
      employeeNode = elementList.item(2);
      employeeList = employeeNode.getChildNodes();
      length = (int) employeeList.getLength();
      child = employeeList.item(0);
      childName = child.getNodeName();
      if (equals(13, length)) {
          assertEquals("childName_w_whitespace", "#text", childName);
      } else {
          assertEqualsAutoCase("element", "childName_wo_whitespace", "em", childName);
        }
    }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_nodelistindexequalzero.class, args);
   }
}
