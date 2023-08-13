public final class hc_textsplittextfour extends DOMTestCase {
   public hc_textsplittextfour(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node addressNode;
      Text textNode;
      Text splitNode;
      String value;
      doc = (Document) load("hc_staff", true);
      elementList = doc.getElementsByTagName("acronym");
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
        DOMTestCase.doMain(hc_textsplittextfour.class, args);
   }
}
