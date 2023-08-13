public final class hc_nodeparentnode extends DOMTestCase {
   public hc_nodeparentnode(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node employeeNode;
      Node parentNode;
      String parentName;
      doc = (Document) load("hc_staff", false);
      elementList = doc.getElementsByTagName("p");
      employeeNode = elementList.item(1);
      parentNode = employeeNode.getParentNode();
      parentName = parentNode.getNodeName();
      assertEqualsAutoCase("element", "parentNodeName", "body", parentName);
        }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_nodeparentnode.class, args);
   }
}
