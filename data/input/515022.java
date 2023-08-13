public final class elementhasattributens02 extends DOMTestCase {
   public elementhasattributens02(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      org.w3c.domts.DocumentBuilderSetting[] settings = 
          new org.w3c.domts.DocumentBuilderSetting[] {
org.w3c.domts.DocumentBuilderSetting.namespaceAware
        };
        DOMTestDocumentBuilderFactory testFactory = factory.newInstance(settings);
        setFactory(testFactory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      Element element;
      boolean state;
      Attr attribute;
      Attr newAttribute;
      doc = (Document) load("staff", false);
      element = doc.createElementNS("http:
      attribute = doc.createAttributeNS("http:
      newAttribute = element.setAttributeNode(attribute);
      state = element.hasAttributeNS("http:
      assertTrue("hasDomesticAttr", state);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(elementhasattributens02.class, args);
   }
}
