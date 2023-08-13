public final class elementgetattributenodens01 extends DOMTestCase {
   public elementgetattributenodens01(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      Element element;
      Attr attribute1;
      Attr attribute2;
      Attr newAttribute1;
      Attr newAttribute2;
      Attr attribute;
      String attrValue;
      String attrName;
      String attNodeName;
      String attrLocalName;
      String attrNS;
      doc = (Document) load("staffNS", false);
      element = doc.createElementNS("namespaceURI", "root");
      attribute1 = doc.createAttributeNS("http:
      newAttribute1 = element.setAttributeNodeNS(attribute1);
      attribute2 = doc.createAttributeNS("http:
      newAttribute2 = element.setAttributeNodeNS(attribute2);
      attribute = element.getAttributeNodeNS("http:
      attrValue = attribute.getNodeValue();
      attrName = attribute.getName();
      attNodeName = attribute.getNodeName();
      attrLocalName = attribute.getLocalName();
      attrNS = attribute.getNamespaceURI();
      assertEquals("elementgetattributenodens01_attrValue", "", attrValue);
      assertEquals("elementgetattributenodens01_attrName", "l2:att", attrName);
      assertEquals("elementgetattributenodens01_attrNodeName", "l2:att", attNodeName);
      assertEquals("elementgetattributenodens01_attrLocalName", "att", attrLocalName);
      assertEquals("elementgetattributenodens01_attrNs", "http:
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(elementgetattributenodens01.class, args);
   }
}
