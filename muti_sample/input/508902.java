public final class hc_textsplittextthree extends DOMTestCase {
   public hc_textsplittextthree(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node nameNode;
      Text textNode;
      Text splitNode;
      String value;
      doc = (Document) load("hc_staff", true);
      elementList = doc.getElementsByTagName("strong");
      nameNode = elementList.item(2);
      textNode = (Text) nameNode.getFirstChild();
      splitNode = textNode.splitText(6);
      value = splitNode.getNodeValue();
      assertEquals("textSplitTextThreeAssert", " Jones", value);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_textsplittextthree.class, args);
   }
}
