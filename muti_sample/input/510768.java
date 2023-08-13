public final class namespaceURI01 extends DOMTestCase {
   public namespaceURI01(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      org.w3c.domts.DocumentBuilderSetting[] settings = 
          new org.w3c.domts.DocumentBuilderSetting[] {
org.w3c.domts.DocumentBuilderSetting.namespaceAware,
org.w3c.domts.DocumentBuilderSetting.validating
        };
        DOMTestDocumentBuilderFactory testFactory = factory.newInstance(settings);
        setFactory(testFactory);
    String contentType = getContentType();
    preload(contentType, "staffNS", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Element testAddr;
      Attr addrAttr;
      String attrNamespaceURI;
      doc = (Document) load("staffNS", false);
      elementList = doc.getElementsByTagName("emp:address");
      testAddr = (Element) elementList.item(0);
      addrAttr = testAddr.getAttributeNodeNS("http:
      attrNamespaceURI = addrAttr.getNamespaceURI();
      assertEquals("namespaceURI", "http:
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(namespaceURI01.class, args);
   }
}
