public final class hc_nodegetprevioussiblingnull extends DOMTestCase {
   public hc_nodegetprevioussiblingnull(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node employeeNode;
      Node fcNode;
      Node psNode;
      doc = (Document) load("hc_staff", false);
      elementList = doc.getElementsByTagName("p");
      employeeNode = elementList.item(2);
      fcNode = employeeNode.getFirstChild();
      psNode = fcNode.getPreviousSibling();
      assertNull("nodeGetPreviousSiblingNullAssert1", psNode);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_nodegetprevioussiblingnull.class, args);
   }
}
