public final class hc_nodeattributenodevalue extends DOMTestCase {
   public hc_nodeattributenodevalue(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Element testAddr;
      Attr addrAttr;
      String attrValue;
      doc = (Document) load("hc_staff", false);
      elementList = doc.getElementsByTagName("acronym");
      testAddr = (Element) elementList.item(0);
      addrAttr = testAddr.getAttributeNode("title");
      attrValue = addrAttr.getNodeValue();
      assertEquals("nodeValue", "Yes", attrValue);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_nodeattributenodevalue.class, args);
   }
}
