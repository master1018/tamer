public final class namednodemapsetnameditemns02 extends DOMTestCase {
   public namednodemapsetnameditemns02(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      Attr attribute1;
      Node newNode;
      String attrName;
      doc = (Document) load("staffNS", true);
      element = doc.createElementNS("http:
      attribute1 = doc.createAttributeNS("http:
      attributes = element.getAttributes();
      newNode = attributes.setNamedItemNS(attribute1);
      attribute = (Attr) attributes.getNamedItemNS("http:
      attrName = attribute.getNodeName();
      assertEquals("namednodemapsetnameditemns02", "L1:att", attrName);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(namednodemapsetnameditemns02.class, args);
   }
}
