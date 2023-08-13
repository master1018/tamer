public final class attreffectivevalue extends DOMTestCase {
   public attreffectivevalue(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList addressList;
      Node testNode;
      NamedNodeMap attributes;
      Attr domesticAttr;
      String value;
      doc = (Document) load("staff", false);
      addressList = doc.getElementsByTagName("address");
      testNode = addressList.item(0);
      attributes = testNode.getAttributes();
      domesticAttr = (Attr) attributes.getNamedItem("domestic");
      value = domesticAttr.getNodeValue();
      assertEquals("attrEffectiveValueAssert", "Yes", value);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(attreffectivevalue.class, args);
   }
}
