public final class hc_nodeclonenodefalse extends DOMTestCase {
   public hc_nodeclonenodefalse(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node employeeNode;
      Node clonedNode;
      String cloneName;
      NodeList cloneChildren;
      int length;
      doc = (Document) load("hc_staff", true);
      elementList = doc.getElementsByTagName("p");
      employeeNode = elementList.item(1);
      clonedNode = employeeNode.cloneNode(false);
      cloneName = clonedNode.getNodeName();
      assertEqualsAutoCase("element", "strong", "p", cloneName);
        cloneChildren = clonedNode.getChildNodes();
      length = (int) cloneChildren.getLength();
      assertEquals("length", 0, length);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_nodeclonenodefalse.class, args);
   }
}
