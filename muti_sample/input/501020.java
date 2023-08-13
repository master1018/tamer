public final class elementgetattributenodens02 extends DOMTestCase {
   public elementgetattributenodens02(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      Attr attribute;
      Attr newAttribute1;
      String attrValue;
      doc = (Document) load("staffNS", false);
      element = doc.createElementNS("namespaceURI", "root");
      attribute = doc.createAttributeNS("http:
      newAttribute1 = element.setAttributeNodeNS(attribute);
      attribute = element.getAttributeNodeNS("http:
      attrValue = attribute.getNodeValue();
      assertEquals("elementgetattributenodens02", "", attrValue);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(elementgetattributenodens02.class, args);
   }
}
