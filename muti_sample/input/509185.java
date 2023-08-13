public final class namednodemapsetnameditemns01 extends DOMTestCase {
   public namednodemapsetnameditemns01(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      Attr newAttribute;
      Attr newAttr1;
      NodeList elementList;
      String attrName;
      doc = (Document) load("staffNS", true);
      elementList = doc.getElementsByTagNameNS("http:
      element = elementList.item(0);
      attributes = element.getAttributes();
      newAttr1 = doc.createAttributeNS("http:
      newAttribute = ((Element) element).setAttributeNodeNS(newAttr1);
      attribute = (Attr) attributes.getNamedItemNS("http:
      attrName = attribute.getNodeName();
      assertEquals("namednodemapsetnameditemns01", "streets", attrName);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(namednodemapsetnameditemns01.class, args);
   }
}
