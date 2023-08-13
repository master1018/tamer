public final class namednodemapremovenameditemns02 extends DOMTestCase {
   public namednodemapremovenameditemns02(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      org.w3c.domts.DocumentBuilderSetting[] settings = 
          new org.w3c.domts.DocumentBuilderSetting[] {
org.w3c.domts.DocumentBuilderSetting.namespaceAware,
org.w3c.domts.DocumentBuilderSetting.validating
        };
        DOMTestDocumentBuilderFactory testFactory = factory.newInstance(settings);
        setFactory(testFactory);
    String contentType = getContentType();
    preload(contentType, "staffNS", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NamedNodeMap attributes;
      Node element;
      Attr attribute;
      NodeList elementList;
      String attrValue;
      String nullNS = null;
      doc = (Document) load("staffNS", true);
      elementList = doc.getElementsByTagNameNS("http:
      element = elementList.item(1);
      attributes = element.getAttributes();
      attribute = (Attr) attributes.removeNamedItemNS(nullNS, "defaultAttr");
      attribute = (Attr) attributes.getNamedItemNS(nullNS, "defaultAttr");
      attrValue = attribute.getNodeValue();
      assertNotNull("namednodemapremovenameditemns02", attribute);
      assertEquals("namednodemapremovenameditemns02_attrValue", "defaultVal", attrValue);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(namednodemapremovenameditemns02.class, args);
   }
}
