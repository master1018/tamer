public final class nodeattributenodevalue extends DOMTestCase {
   public nodeattributenodevalue(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Element testAddr;
      Attr addrAttr;
      String attrValue;
      doc = (Document) load("staff", false);
      elementList = doc.getElementsByTagName("address");
      testAddr = (Element) elementList.item(0);
      addrAttr = testAddr.getAttributeNode("domestic");
      attrValue = addrAttr.getNodeValue();
      assertEquals("nodeAttributeNodeValueAssert1", "Yes", attrValue);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodeattributenodevalue.class, args);
   }
}
