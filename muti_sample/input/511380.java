public final class hc_nodeattributenodename extends DOMTestCase {
   public hc_nodeattributenodename(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Element testAddr;
      Attr addrAttr;
      String attrName;
      doc = (Document) load("hc_staff", false);
      elementList = doc.getElementsByTagName("acronym");
      testAddr = (Element) elementList.item(0);
      addrAttr = testAddr.getAttributeNode("title");
      attrName = addrAttr.getNodeName();
      assertEqualsAutoCase("attribute", "nodeName", "title", attrName);
        }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_nodeattributenodename.class, args);
   }
}
