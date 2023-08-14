public final class namednodemapgetnameditemns02 extends DOMTestCase {
   public namednodemapgetnameditemns02(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      NamedNodeMap attributes;
      Node element;
      Attr attribute;
      NodeList elementList;
      String attrName;
      doc = (Document) load("staffNS", false);
      elementList = doc.getElementsByTagNameNS("http:
      element = elementList.item(1);
      attributes = element.getAttributes();
      attribute = (Attr) attributes.getNamedItemNS("http:
      attrName = attribute.getNodeName();
      assertEquals("namednodemapgetnameditemns02", "emp:domestic", attrName);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(namednodemapgetnameditemns02.class, args);
   }
}
