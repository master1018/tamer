public final class hc_nodechildnodesempty extends DOMTestCase {
   public hc_nodechildnodesempty(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      NodeList childList;
      Node employeeNode;
      Node textNode;
      int length;
      doc = (Document) load("hc_staff", false);
      elementList = doc.getElementsByTagName("em");
      employeeNode = elementList.item(1);
      textNode = employeeNode.getFirstChild();
      childList = textNode.getChildNodes();
      length = (int) childList.getLength();
      assertEquals("length_zero", 0, length);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_nodechildnodesempty.class, args);
   }
}
