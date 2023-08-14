public final class namednodemapgetnameditemns03 extends DOMTestCase {
   public namednodemapgetnameditemns03(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      Attr newAttr1;
      Attr newAttr2;
      Attr newAttribute;
      String attrName;
      doc = (Document) load("staffNS", false);
      element = doc.createElementNS("http:
      newAttr1 = doc.createAttributeNS("http:
      newAttribute = ((Element) element).setAttributeNodeNS(newAttr1);
      newAttr2 = doc.createAttributeNS("http:
      newAttribute = ((Element) element).setAttributeNodeNS(newAttr2);
      attributes = element.getAttributes();
      attribute = (Attr) attributes.getNamedItemNS("http:
      attrName = attribute.getNodeName();
      assertEquals("namednodemapgetnameditemns03", "L2:att", attrName);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(namednodemapgetnameditemns03.class, args);
   }
}
