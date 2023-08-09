public final class setAttributeNodeNS04 extends DOMTestCase {
   public setAttributeNodeNS04(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      Attr newAttr;
      Attr newAddrAttr;
      String newName;
      doc = (Document) load("staffNS", true);
      elementList = doc.getElementsByTagName("emp:address");
      testAddr = elementList.item(0);
      assertNotNull("empAddrNotNull", testAddr);
      newAttr = doc.createAttributeNS("http:
      newAddrAttr = ((Element) testAddr).setAttributeNodeNS(newAttr);
      newName = newAddrAttr.getNodeName();
      assertEquals("nodeName", "emp:domestic", newName);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(setAttributeNodeNS04.class, args);
   }
}
