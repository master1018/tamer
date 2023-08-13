public final class nodeattributenodename extends DOMTestCase {
   public nodeattributenodename(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Element testAddr;
      Attr addrAttr;
      String attrName;
      doc = (Document) load("staff", false);
      elementList = doc.getElementsByTagName("address");
      testAddr = (Element) elementList.item(0);
      addrAttr = testAddr.getAttributeNode("domestic");
      attrName = addrAttr.getNodeName();
      assertEquals("nodeAttributeNodeNameAssert1", "domestic", attrName);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodeattributenodename.class, args);
   }
}
