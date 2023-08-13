public final class getElementsByTagNameNS07 extends DOMTestCase {
   public getElementsByTagNameNS07(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      NodeList elementList;
      doc = (Document) load("staffNS", false);
      elementList = doc.getElementsByTagNameNS("http:
      assertSize("addresses", 3, elementList);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(getElementsByTagNameNS07.class, args);
   }
}
