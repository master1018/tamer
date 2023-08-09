public final class namednodemapremovenameditemns04 extends DOMTestCase {
   public namednodemapremovenameditemns04(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      NamedNodeMap attributes;
      Node element;
      Attr attribute;
      Attr attributeRemoved;
      NodeList elementList;
      doc = (Document) load("staffNS", true);
      elementList = doc.getElementsByTagNameNS("*", "employee");
      element = elementList.item(0);
      attributes = element.getAttributes();
      attributeRemoved = (Attr) attributes.removeNamedItemNS("http:
      attribute = (Attr) attributes.getNamedItemNS("http:
      assertNull("namednodemapremovenameditemns04_1", attribute);
      attributeRemoved = (Attr) attributes.removeNamedItemNS("http:
      attribute = (Attr) attributes.getNamedItemNS("http:
      assertNull("namednodemapremovenameditemns04_2", attribute);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(namednodemapremovenameditemns04.class, args);
   }
}
