public final class namednodemapremovenameditemns09 extends DOMTestCase {
   public namednodemapremovenameditemns09(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      NamedNodeMap newAttributes;
      Element element;
      Attr attribute;
      NodeList elementList;
      doc = (Document) load("staffNS", true);
      elementList = doc.getElementsByTagNameNS("http:
      element = (Element) elementList.item(1);
      attributes = element.getAttributes();
      attribute = (Attr) attributes.removeNamedItemNS("http:
      newAttributes = element.getAttributes();
      attribute = (Attr) newAttributes.getNamedItemNS("http:
      assertNull("namednodemapremovenameditemns09", attribute);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(namednodemapremovenameditemns09.class, args);
   }
}
