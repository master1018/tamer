public final class nodeclonefalsenocopytext extends DOMTestCase {
   public nodeclonefalsenocopytext(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node employeeNode;
      NodeList childList;
      Node childNode;
      Node clonedNode;
      Node lastChildNode;
      doc = (Document) load("staff", true);
      elementList = doc.getElementsByTagName("employee");
      employeeNode = elementList.item(1);
      childList = employeeNode.getChildNodes();
      childNode = childList.item(3);
      clonedNode = childNode.cloneNode(false);
      lastChildNode = clonedNode.getLastChild();
      assertNull("noTextNodes", lastChildNode);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodeclonefalsenocopytext.class, args);
   }
}
