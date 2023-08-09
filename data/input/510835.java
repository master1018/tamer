public final class nodehaschildnodesfalse extends DOMTestCase {
   public nodehaschildnodesfalse(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node child;
      NodeList employeeIdList;
      Node employeeNode;
      Node textNode;
      boolean state;
      doc = (Document) load("staff", false);
      elementList = doc.getElementsByTagName("employee");
      child = elementList.item(1);
      employeeIdList = child.getChildNodes();
      employeeNode = employeeIdList.item(1);
      textNode = employeeNode.getFirstChild();
      state = textNode.hasChildNodes();
      assertFalse("nodeHasChildFalseAssert1", state);
}
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodehaschildnodesfalse.class, args);
   }
}
