public final class nodeclonenodefalse extends DOMTestCase {
   public nodeclonenodefalse(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node employeeNode;
      Node clonedNode;
      String cloneName;
      NodeList cloneChildren;
      int length;
      doc = (Document) load("staff", true);
      elementList = doc.getElementsByTagName("employee");
      employeeNode = elementList.item(1);
      clonedNode = employeeNode.cloneNode(false);
      cloneName = clonedNode.getNodeName();
      assertEquals("name", "employee", cloneName);
      cloneChildren = clonedNode.getChildNodes();
      length = (int) cloneChildren.getLength();
      assertEquals("length", 0, length);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodeclonenodefalse.class, args);
   }
}
