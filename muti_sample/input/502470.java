public final class hc_nodeclonefalsenocopytext extends DOMTestCase {
   public hc_nodeclonefalsenocopytext(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node employeeNode;
      NodeList childList;
      Node childNode;
      Node clonedNode;
      Node lastChildNode;
      doc = (Document) load("hc_staff", true);
      elementList = doc.getElementsByTagName("p");
      employeeNode = elementList.item(1);
      childList = employeeNode.getChildNodes();
      childNode = childList.item(3);
      clonedNode = childNode.cloneNode(false);
      lastChildNode = clonedNode.getLastChild();
      assertNull("nodeCloneFalseNoCopyTextAssert1", lastChildNode);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_nodeclonefalsenocopytext.class, args);
   }
}
