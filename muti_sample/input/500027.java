public final class elementhasattributens01 extends DOMTestCase {
   public elementhasattributens01(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      org.w3c.domts.DocumentBuilderSetting[] settings = 
          new org.w3c.domts.DocumentBuilderSetting[] {
org.w3c.domts.DocumentBuilderSetting.namespaceAware
        };
        DOMTestDocumentBuilderFactory testFactory = factory.newInstance(settings);
        setFactory(testFactory);
    String contentType = getContentType();
    preload(contentType, "staffNS", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      Element element;
      boolean state;
      NodeList elementList;
      doc = (Document) load("staffNS", false);
      elementList = doc.getElementsByTagNameNS("*", "employee");
      element = (Element) elementList.item(0);
      state = element.hasAttributeNS("http:
      assertTrue("elementhasattributens01", state);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(elementhasattributens01.class, args);
   }
}
