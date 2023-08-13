public final class nodelistindexgetlength extends DOMTestCase {
   public nodelistindexgetlength(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node employeeNode;
      NodeList employeeList;
      int length;
      int expectedCount = 0;
      doc = (Document) load("staff", false);
      elementList = doc.getElementsByTagName("employee");
      employeeNode = elementList.item(2);
      employeeList = employeeNode.getChildNodes();
      length = (int) employeeList.getLength();
      assertTrue("lengthIs6or13", 
    (equals(6, length) | equals(13, length))
);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodelistindexgetlength.class, args);
   }
}
