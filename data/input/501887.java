public final class setAttributeNS04 extends DOMTestCase {
   public setAttributeNS04(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      org.w3c.domts.DocumentBuilderSetting[] settings = 
          new org.w3c.domts.DocumentBuilderSetting[] {
org.w3c.domts.DocumentBuilderSetting.namespaceAware
        };
        DOMTestDocumentBuilderFactory testFactory = factory.newInstance(settings);
        setFactory(testFactory);
    String contentType = getContentType();
    preload(contentType, "staffNS", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node testAddr;
      Attr addrAttr;
      String resultAttr;
      String resultNamespaceURI;
      String resultLocalName;
      String resultPrefix;
      doc = (Document) load("staffNS", true);
      elementList = doc.getElementsByTagName("emp:address");
      testAddr = elementList.item(0);
      assertNotNull("empAddrNotNull", testAddr);
      ((Element) testAddr).setAttributeNS("http:
      addrAttr = ((Element) testAddr).getAttributeNodeNS("http:
      resultAttr = ((Element) testAddr).getAttributeNS("http:
      assertEquals("attrValue", "newValue", resultAttr);
      resultNamespaceURI = addrAttr.getNamespaceURI();
      assertEquals("nsuri", "http:
      resultLocalName = addrAttr.getLocalName();
      assertEquals("lname", "zone", resultLocalName);
      resultPrefix = addrAttr.getPrefix();
      assertEquals("prefix", "newprefix", resultPrefix);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(setAttributeNS04.class, args);
   }
}
