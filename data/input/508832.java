public final class elementsetattributenodens02 extends DOMTestCase {
   public elementsetattributenodens02(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      Element element;
      Element element2;
      Attr attribute;
      Attr attributeCloned;
      Attr newAttr;
      NodeList elementList;
      String attrName;
      String attrValue;
      String nullNS = null;
      doc = (Document) load("staffNS", true);
      elementList = doc.getElementsByTagNameNS("http:
      element = (Element) elementList.item(1);
      attribute = element.getAttributeNodeNS(nullNS, "street");
      attributeCloned = (Attr) attribute.cloneNode(true);
      element2 = (Element) elementList.item(2);
      newAttr = element2.setAttributeNodeNS(attributeCloned);
      attrName = newAttr.getNodeName();
      attrValue = newAttr.getNodeValue();
      assertEquals("elementsetattributenodens02_attrName", "street", attrName);
      assertEquals("elementsetattributenodens02_attrValue", "Yes", attrValue);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(elementsetattributenodens02.class, args);
   }
}
