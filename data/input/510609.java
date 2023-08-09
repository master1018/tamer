public final class hasAttributeNS04 extends DOMTestCase {
   public hasAttributeNS04(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      String localName = "district";
      String namespaceURI = "http:
      Document doc;
      NodeList elementList;
      Element testNode;
      boolean state;
      doc = (Document) load("staffNS", false);
      elementList = doc.getElementsByTagName("emp:address");
      testNode = (Element) elementList.item(0);
      assertNotNull("empAddressNotNull", testNode);
      state = testNode.hasAttributeNS(namespaceURI, localName);
      assertTrue("hasAttribute", state);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hasAttributeNS04.class, args);
   }
}
