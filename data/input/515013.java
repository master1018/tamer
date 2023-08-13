public final class hc_nodegetlastchild extends DOMTestCase {
   public hc_nodegetlastchild(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node employeeNode;
      Node lchildNode;
      String childName;
      doc = (Document) load("hc_staff", false);
      elementList = doc.getElementsByTagName("p");
      employeeNode = elementList.item(1);
      lchildNode = employeeNode.getLastChild();
      childName = lchildNode.getNodeName();
      assertEquals("whitespace", "#text", childName);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_nodegetlastchild.class, args);
   }
}
