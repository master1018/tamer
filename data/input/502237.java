public final class hc_nodeattributenodetype extends DOMTestCase {
   public hc_nodeattributenodetype(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Element testAddr;
      Attr addrAttr;
      int nodeType;
      doc = (Document) load("hc_staff", false);
      elementList = doc.getElementsByTagName("acronym");
      testAddr = (Element) elementList.item(0);
      addrAttr = testAddr.getAttributeNode("title");
      nodeType = (int) addrAttr.getNodeType();
      assertEquals("nodeAttrNodeTypeAssert1", 2, nodeType);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_nodeattributenodetype.class, args);
   }
}
