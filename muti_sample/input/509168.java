public final class hc_nodelistindexnotzero extends DOMTestCase {
   public hc_nodelistindexnotzero(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      doc = (Document) load("hc_staff", false);
      elementList = doc.getElementsByTagName("p");
      employeeNode = elementList.item(2);
      employeeList = employeeNode.getChildNodes();
      child = employeeList.item(3);
      childName = child.getNodeName();
      if (equals("#text", childName)) {
          assertEquals("childName_space", "#text", childName);
      } else {
          assertEqualsAutoCase("element", "childName_strong", "strong", childName);
        }
    }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_nodelistindexnotzero.class, args);
   }
}
