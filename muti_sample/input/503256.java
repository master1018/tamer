public final class elementremoveattributens01 extends DOMTestCase {
   public elementremoveattributens01(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      newAttribute = element.setAttributeNodeNS(attribute);
      element.removeAttributeNS("http:
      state = element.hasAttributeNS("http:
      assertFalse("elementremoveattributens01", state);
}
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(elementremoveattributens01.class, args);
   }
}
