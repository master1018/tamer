public final class localName01 extends DOMTestCase {
   public localName01(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      org.w3c.domts.DocumentBuilderSetting[] settings = 
          new org.w3c.domts.DocumentBuilderSetting[] {
org.w3c.domts.DocumentBuilderSetting.namespaceAware
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
      String localName;
      doc = (Document) load("staffNS", false);
      elementList = doc.getElementsByTagName("emp:address");
      testAddr = (Element) elementList.item(0);
      assertNotNull("empAddrNotNull", testAddr);
      addrAttr = testAddr.getAttributeNode("emp:domestic");
      localName = addrAttr.getLocalName();
      assertEquals("localName", "domestic", localName);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(localName01.class, args);
   }
}
