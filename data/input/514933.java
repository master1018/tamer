public final class getNamedItemNS01 extends DOMTestCase {
   public getNamedItemNS01(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      Node testEmployee;
      NamedNodeMap attributes;
      Attr domesticAttr;
      String attrName;
      doc = (Document) load("staffNS", false);
      elementList = doc.getElementsByTagName("address");
      testEmployee = elementList.item(1);
      attributes = testEmployee.getAttributes();
      domesticAttr = (Attr) attributes.getNamedItemNS("http:
      attrName = domesticAttr.getNodeName();
      assertEquals("attrName", "dmstc:domestic", attrName);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(getNamedItemNS01.class, args);
   }
}
