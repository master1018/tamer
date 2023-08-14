public final class hc_nodegetnextsibling extends DOMTestCase {
   public hc_nodegetnextsibling(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node emNode;
      Node nsNode;
      String nsName;
      doc = (Document) load("hc_staff", false);
      elementList = doc.getElementsByTagName("em");
      emNode = elementList.item(1);
      nsNode = emNode.getNextSibling();
      nsName = nsNode.getNodeName();
      assertEquals("whitespace", "#text", nsName);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_nodegetnextsibling.class, args);
   }
}
