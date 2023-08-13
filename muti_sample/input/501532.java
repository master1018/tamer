public final class normalize01 extends DOMTestCase {
   public normalize01(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      Element root;
      NodeList elementList;
      Node firstChild;
      NodeList textList;
      CharacterData textNode;
      String data;
      doc = (Document) load("staff", false);
      root = doc.getDocumentElement();
      root.normalize();
      elementList = root.getElementsByTagName("name");
      firstChild = elementList.item(2);
      textList = firstChild.getChildNodes();
      textNode = (CharacterData) textList.item(0);
      data = textNode.getData();
      assertEquals("data", "Roger\n Jones", data);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(normalize01.class, args);
   }
}
