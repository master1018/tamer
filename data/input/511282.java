public final class elementsetattributens01 extends DOMTestCase {
   public elementsetattributens01(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      Attr attribute;
      String attrName;
      String attrValue;
      doc = (Document) load("staff", true);
      element = doc.createElementNS("http:
      element.setAttributeNS("http:
      attribute = element.getAttributeNodeNS("http:
      attrName = attribute.getNodeName();
      attrValue = attribute.getNodeValue();
      assertEquals("elementsetattributens01_attrName", "attr", attrName);
      assertEquals("elementsetattributens01_attrValue", "value", attrValue);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(elementsetattributens01.class, args);
   }
}
