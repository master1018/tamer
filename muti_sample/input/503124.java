public final class elementgetattributenodens03 extends DOMTestCase {
   public elementgetattributenodens03(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      org.w3c.domts.DocumentBuilderSetting[] settings = 
          new org.w3c.domts.DocumentBuilderSetting[] {
org.w3c.domts.DocumentBuilderSetting.namespaceAware,
org.w3c.domts.DocumentBuilderSetting.validating
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
      String attrValue;
      NodeList childList;
      String nullNS = null;
      doc = (Document) load("staffNS", false);
      childList = doc.getElementsByTagNameNS("http:
      element = (Element) childList.item(1);
      attribute = element.getAttributeNodeNS(nullNS, "defaultAttr");
      attrValue = attribute.getNodeValue();
      assertEquals("elementgetattributenodens03", "defaultVal", attrValue);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(elementgetattributenodens03.class, args);
   }
}
