public final class hasAttributeNS05 extends DOMTestCase {
   public hasAttributeNS05(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      String localName = "domestic";
      String namespaceURI = "http:
      Document doc;
      NodeList elementList;
      Element testNode;
      boolean state;
      doc = (Document) load("staffNS", false);
      elementList = doc.getElementsByTagName("address");
      testNode = (Element) elementList.item(0);
      state = testNode.hasAttributeNS(namespaceURI, localName);
      assertTrue("hasAttribute", state);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hasAttributeNS05.class, args);
   }
}
