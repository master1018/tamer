public final class setNamedItemNS01 extends DOMTestCase {
   public setNamedItemNS01(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      Node anotherElement;
      NamedNodeMap anotherMap;
      Node arg;
      Node testAddress;
      NamedNodeMap map;
      Node setNode;
      doc = (Document) load("staffNS", true);
      elementList = doc.getElementsByTagName("address");
      anotherElement = elementList.item(2);
      anotherMap = anotherElement.getAttributes();
      arg = anotherMap.getNamedItemNS("http:
      testAddress = elementList.item(0);
      map = testAddress.getAttributes();
      {
         boolean success = false;
         try {
            setNode = map.setNamedItemNS(arg);
          } catch (DOMException ex) {
            success = (ex.code == DOMException.INUSE_ATTRIBUTE_ERR);
         }
         assertTrue("throw_INUSE_ATTRIBUTE_ERR", success);
      }
}
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(setNamedItemNS01.class, args);
   }
}
