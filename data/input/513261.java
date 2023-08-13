public final class namednodemapgetnameditemns06 extends DOMTestCase {
   public namednodemapgetnameditemns06(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      NamedNodeMap attributesMap1;
      NamedNodeMap attributesMap2;
      Element element;
      Attr attribute;
      Attr newAttr1;
      Attr newAttribute;
      NodeList elementList;
      String attrName;
      doc = (Document) load("staffNS", true);
      elementList = doc.getElementsByTagNameNS("*", "address");
      element = (Element) elementList.item(1);
      attributesMap1 = element.getAttributes();
      attributesMap2 = element.getAttributes();
      newAttr1 = doc.createAttributeNS("http:
      newAttribute = element.setAttributeNodeNS(newAttr1);
      attribute = (Attr) attributesMap1.getNamedItemNS("http:
      attrName = attribute.getNodeName();
      assertEquals("namednodemapgetnameditemnsMap106", "street", attrName);
      attribute = (Attr) attributesMap2.getNamedItemNS("http:
      attrName = attribute.getNodeName();
      assertEquals("namednodemapgetnameditemnsMap206", "street", attrName);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(namednodemapgetnameditemns06.class, args);
   }
}
