public final class elementsetattributens02 extends DOMTestCase {
   public elementsetattributens02(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      NodeList elementList;
      String attrName;
      String attrValue;
      doc = (Document) load("staff", true);
      elementList = doc.getElementsByTagNameNS("*", "address");
      element = (Element) elementList.item(0);
      element.setAttributeNS("http:
      attribute = element.getAttributeNodeNS("http:
      attrName = attribute.getNodeName();
      attrValue = attribute.getNodeValue();
      assertEquals("elementsetattributens02_attrName", "this:street", attrName);
      assertEquals("elementsetattributens02_attrValue", "Silver Street", attrValue);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(elementsetattributens02.class, args);
   }
}
