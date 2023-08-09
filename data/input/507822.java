public final class elementsetattributens03 extends DOMTestCase {
   public elementsetattributens03(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      Attr attribute;
      NodeList elementList;
      String attrName;
      String attrValue;
      doc = (Document) load("staffNS", true);
      elementList = doc.getElementsByTagName("emp:employee");
      element = (Element) elementList.item(0);
      assertNotNull("empEmployeeNotNull", element);
      element.setAttributeNS("http:
      element.setAttributeNS("http:
      attribute = element.getAttributeNodeNS("http:
      attrName = attribute.getNodeName();
      attrValue = attribute.getNodeValue();
      assertEquals("elementsetattributens03_attrName", "defaultAttr", attrName);
      assertEquals("elementsetattributens03_attrValue", "default1", attrValue);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(elementsetattributens03.class, args);
   }
}
