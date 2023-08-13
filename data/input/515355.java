public final class nodeparentnode extends DOMTestCase {
   public nodeparentnode(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node employeeNode;
      Node parentNode;
      String parentName;
      doc = (Document) load("staff", false);
      elementList = doc.getElementsByTagName("employee");
      employeeNode = elementList.item(1);
      parentNode = employeeNode.getParentNode();
      parentName = parentNode.getNodeName();
      if (("image/svg+xml".equals(getContentType()))) {
          assertEquals("svgTagName", "svg", parentName);
      } else {
          assertEquals("nodeParentNodeAssert1", "staff", parentName);
      }
    }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodeparentnode.class, args);
   }
}
