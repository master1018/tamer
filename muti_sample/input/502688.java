public final class hc_nodegetprevioussibling extends DOMTestCase {
   public hc_nodegetprevioussibling(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node nameNode;
      Node psNode;
      String psName;
      doc = (Document) load("hc_staff", false);
      elementList = doc.getElementsByTagName("strong");
      nameNode = elementList.item(1);
      psNode = nameNode.getPreviousSibling();
      psName = psNode.getNodeName();
      assertEquals("whitespace", "#text", psName);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_nodegetprevioussibling.class, args);
   }
}
