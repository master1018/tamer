public final class hc_nodetextnodevalue extends DOMTestCase {
   public hc_nodetextnodevalue(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Element testAddr;
      Node textNode;
      String textValue;
      doc = (Document) load("hc_staff", false);
      elementList = doc.getElementsByTagName("acronym");
      testAddr = (Element) elementList.item(0);
      textNode = testAddr.getFirstChild();
      textValue = textNode.getNodeValue();
      assertEquals("textNodeValue", "1230 North Ave. Dallas, Texas 98551", textValue);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_nodetextnodevalue.class, args);
   }
}
