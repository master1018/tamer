public final class textwithnomarkup extends DOMTestCase {
   public textwithnomarkup(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node nameNode;
      Node nodeV;
      String value;
      doc = (Document) load("staff", false);
      elementList = doc.getElementsByTagName("name");
      nameNode = elementList.item(2);
      nodeV = nameNode.getFirstChild();
      value = nodeV.getNodeValue();
      assertEquals("textNodeValue", "Roger\n Jones", value);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(textwithnomarkup.class, args);
   }
}
