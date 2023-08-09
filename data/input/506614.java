public final class attrentityreplacement extends DOMTestCase {
   public attrentityreplacement(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList addressList;
      Node testNode;
      NamedNodeMap attributes;
      Attr streetAttr;
      String value;
      doc = (Document) load("staff", true);
      addressList = doc.getElementsByTagName("address");
      testNode = addressList.item(3);
      attributes = testNode.getAttributes();
      streetAttr = (Attr) attributes.getNamedItem("street");
      value = streetAttr.getValue();
      assertEquals("streetYes", "Yes", value);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(attrentityreplacement.class, args);
   }
}
