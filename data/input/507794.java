public final class elementsetattributenodens01 extends DOMTestCase {
   public elementsetattributenodens01(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      org.w3c.domts.DocumentBuilderSetting[] settings = 
          new org.w3c.domts.DocumentBuilderSetting[] {
org.w3c.domts.DocumentBuilderSetting.namespaceAware
        };
        DOMTestDocumentBuilderFactory testFactory = factory.newInstance(settings);
        setFactory(testFactory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      Element element;
      Attr attribute1;
      Attr attribute2;
      Attr attrNode;
      String attrName;
      String attrNS;
      String attrValue;
      NamedNodeMap attributes;
      Attr newAttribute;
      int length;
      doc = (Document) load("staff", true);
      element = doc.createElementNS("http:
      attribute1 = doc.createAttributeNS("http:
      attribute2 = doc.createAttributeNS("http:
      attribute2.setValue("value2");
      newAttribute = element.setAttributeNodeNS(attribute1);
      newAttribute = element.setAttributeNodeNS(attribute2);
      attrNode = element.getAttributeNodeNS("http:
      attrName = attrNode.getNodeName();
      attrNS = attrNode.getNamespaceURI();
      assertEquals("elementsetattributenodens01_attrName", "p2:att", attrName);
      assertEquals("elementsetattributenodens01_attrNS", "http:
      attributes = element.getAttributes();
      length = (int) attributes.getLength();
      assertEquals("length", 1, length);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(elementsetattributenodens01.class, args);
   }
}
