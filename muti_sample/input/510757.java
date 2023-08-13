public final class namednodemapremovenameditemns03 extends DOMTestCase {
   public namednodemapremovenameditemns03(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      Attr attribute1;
      Attr attribute2;
      String nodeName;
      doc = (Document) load("staffNS", true);
      element = doc.createElementNS("http:
      attribute1 = doc.createAttributeNS("http:
      newAttribute = ((Element) element).setAttributeNodeNS(attribute1);
      attribute2 = doc.createAttributeNS("http:
      newAttribute = ((Element) element).setAttributeNodeNS(attribute2);
      attributes = element.getAttributes();
      attribute = (Attr) attributes.removeNamedItemNS("http:
      attribute = (Attr) attributes.getNamedItemNS("http:
      nodeName = attribute.getNodeName();
      assertEquals("namednodemapremovenameditemns02", "L2:att", nodeName);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(namednodemapremovenameditemns03.class, args);
   }
}
