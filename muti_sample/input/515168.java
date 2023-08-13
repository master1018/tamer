public final class hc_nodegetnextsiblingnull extends DOMTestCase {
   public hc_nodegetnextsiblingnull(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node employeeNode;
      Node lcNode;
      Node nsNode;
      doc = (Document) load("hc_staff", false);
      elementList = doc.getElementsByTagName("p");
      employeeNode = elementList.item(1);
      lcNode = employeeNode.getLastChild();
      nsNode = lcNode.getNextSibling();
      assertNull("nodeGetNextSiblingNullAssert1", nsNode);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_nodegetnextsiblingnull.class, args);
   }
}
