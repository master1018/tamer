public final class nodesetprefix02 extends DOMTestCase {
   public nodesetprefix02(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      Attr newAttribute;
      Node setNode;
      NodeList elementList;
      String attrName;
      String newAttrName;
      doc = (Document) load("staffNS", true);
      elementList = doc.getElementsByTagName("address");
      element = (Element) elementList.item(1);
      newAttribute = doc.createAttributeNS("http:
      setNode = element.setAttributeNodeNS(newAttribute);
      newAttribute.setPrefix("dom");
      attribute = element.getAttributeNodeNS("http:
      attrName = attribute.getNodeName();
      newAttrName = newAttribute.getNodeName();
      assertEquals("nodesetprefix02_attrName", "dmstc:domestic", attrName);
      assertEquals("nodesetprefix02_newAttrName", "dom:address", newAttrName);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodesetprefix02.class, args);
   }
}
