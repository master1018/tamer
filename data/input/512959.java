public final class elementhasattributens03 extends DOMTestCase {
   public elementhasattributens03(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      String nullNS = null;
      doc = (Document) load("staff", false);
      element = doc.createElementNS("http:
      assertNotNull("createElementNotNull", element);
      attribute = doc.createAttributeNS(nullNS, "domestic");
      newAttribute = element.setAttributeNode(attribute);
      state = element.hasAttributeNS(nullNS, "domestic");
      assertTrue("elementhasattributens03", state);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(elementhasattributens03.class, args);
   }
}
