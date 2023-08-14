public final class hc_textsplittextone extends DOMTestCase {
   public hc_textsplittextone(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      Node secondPart;
      String value;
      doc = (Document) load("hc_staff", true);
      elementList = doc.getElementsByTagName("strong");
      nameNode = elementList.item(2);
      textNode = (Text) nameNode.getFirstChild();
      splitNode = textNode.splitText(7);
      secondPart = textNode.getNextSibling();
      value = secondPart.getNodeValue();
      assertEquals("textSplitTextOneAssert", "Jones", value);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_textsplittextone.class, args);
   }
}
