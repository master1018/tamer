public final class hc_nodehaschildnodes extends DOMTestCase {
   public hc_nodehaschildnodes(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node employeeNode;
      boolean state;
      doc = (Document) load("hc_staff", false);
      elementList = doc.getElementsByTagName("p");
      employeeNode = elementList.item(1);
      state = employeeNode.hasChildNodes();
      assertTrue("nodeHasChildAssert1", state);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_nodehaschildnodes.class, args);
   }
}
