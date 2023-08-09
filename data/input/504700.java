public final class removeNamedItemNS01 extends DOMTestCase {
   public removeNamedItemNS01(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      Node testAddress;
      NamedNodeMap attributes;
      Attr newAttr;
      Node removedNode;
      doc = (Document) load("staffNS", true);
      elementList = doc.getElementsByTagName("address");
      testAddress = elementList.item(1);
      attributes = testAddress.getAttributes();
      removedNode = attributes.removeNamedItemNS("http:
      assertNotNull("retval", removedNode);
      newAttr = (Attr) attributes.getNamedItem("dmstc:domestic");
      assertNull("nodeRemoved", newAttr);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(removeNamedItemNS01.class, args);
   }
}
