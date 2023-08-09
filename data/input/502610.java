public final class nodegetprevioussiblingnull extends DOMTestCase {
   public nodegetprevioussiblingnull(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node employeeNode;
      Node fcNode;
      Node psNode;
      doc = (Document) load("staff", false);
      elementList = doc.getElementsByTagName("employee");
      employeeNode = elementList.item(2);
      fcNode = employeeNode.getFirstChild();
      psNode = fcNode.getPreviousSibling();
      assertNull("nodeGetPreviousSiblingNullAssert1", psNode);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodegetprevioussiblingnull.class, args);
   }
}
