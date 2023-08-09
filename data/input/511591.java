public final class nodeclonegetparentnull extends DOMTestCase {
   public nodeclonegetparentnull(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node employeeNode;
      Node clonedNode;
      Node parentNode;
      doc = (Document) load("staff", true);
      elementList = doc.getElementsByTagName("employee");
      employeeNode = elementList.item(1);
      clonedNode = employeeNode.cloneNode(false);
      parentNode = clonedNode.getParentNode();
      assertNull("nodeCloneGetParentNullAssert1", parentNode);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodeclonegetparentnull.class, args);
   }
}
