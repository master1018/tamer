public final class namednodemapgetnameditemns05 extends DOMTestCase {
   public namednodemapgetnameditemns05(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      doc = (Document) load("staffNS", false);
      elementList = doc.getElementsByTagNameNS("*", "address");
      element = elementList.item(1);
      attributes = element.getAttributes();
      attribute = (Attr) attributes.getNamedItemNS("*", "street");
      assertNull("namednodemapgetnameditemns05", attribute);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(namednodemapgetnameditemns05.class, args);
   }
}
