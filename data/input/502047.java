public final class nodeattributenodetype extends DOMTestCase {
   public nodeattributenodetype(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Element testAddr;
      Attr addrAttr;
      int nodeType;
      doc = (Document) load("staff", false);
      elementList = doc.getElementsByTagName("address");
      testAddr = (Element) elementList.item(0);
      addrAttr = testAddr.getAttributeNode("domestic");
      nodeType = (int) addrAttr.getNodeType();
      assertEquals("nodeAttrNodeTypeAssert1", 2, nodeType);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodeattributenodetype.class, args);
   }
}
