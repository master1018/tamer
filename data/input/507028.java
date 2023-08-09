public final class elementgetattributens02 extends DOMTestCase {
   public elementgetattributens02(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      String attrValue;
      NodeList childList;
      String nullNS = null;
      doc = (Document) load("staffNS", false);
      childList = doc.getElementsByTagNameNS("http:
      element = (Element) childList.item(1);
      attrValue = element.getAttributeNS(nullNS, "defaultAttr");
      assertEquals("elementgetattributens02", "defaultVal", attrValue);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(elementgetattributens02.class, args);
   }
}
