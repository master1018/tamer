public final class namednodemapgetnameditemns04 extends DOMTestCase {
   public namednodemapgetnameditemns04(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      Element element;
      Attr attribute;
      Attr newAttr1;
      Attr newAttribute;
      NodeList elementList;
      String attrName;
      doc = (Document) load("staffNS", true);
      elementList = doc.getElementsByTagNameNS("*", "address");
      element = (Element) elementList.item(1);
      newAttr1 = doc.createAttributeNS("http:
      newAttribute = element.setAttributeNodeNS(newAttr1);
      attributes = element.getAttributes();
      attribute = (Attr) attributes.getNamedItemNS("http:
      attrName = attribute.getNodeName();
      assertEquals("namednodemapgetnameditemns04", "street", attrName);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(namednodemapgetnameditemns04.class, args);
   }
}
