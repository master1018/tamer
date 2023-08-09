public final class textsplittextfour extends DOMTestCase {
   public textsplittextfour(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node addressNode;
      Text textNode;
      Text splitNode;
      String value;
      doc = (Document) load("staff", true);
      elementList = doc.getElementsByTagName("address");
      addressNode = elementList.item(0);
      textNode = (Text) addressNode.getFirstChild();
      splitNode = textNode.splitText(30);
      value = splitNode.getNodeValue();
      assertEquals("textSplitTextFourAssert", "98551", value);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(textsplittextfour.class, args);
   }
}
