public final class hc_textwithnomarkup extends DOMTestCase {
   public hc_textwithnomarkup(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node nameNode;
      Node nodeV;
      String value;
      doc = (Document) load("hc_staff", false);
      elementList = doc.getElementsByTagName("strong");
      nameNode = elementList.item(2);
      nodeV = nameNode.getFirstChild();
      value = nodeV.getNodeValue();
      assertEquals("textWithNoMarkupAssert", "Roger\n Jones", value);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_textwithnomarkup.class, args);
   }
}
