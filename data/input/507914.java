public final class getElementsByTagNameNS13 extends DOMTestCase {
   public getElementsByTagNameNS13(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      Element docElem;
      NodeList elementList;
      doc = (Document) load("staffNS", false);
      docElem = doc.getDocumentElement();
      elementList = docElem.getElementsByTagNameNS("http:
      assertSize("matchSize", 0, elementList);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(getElementsByTagNameNS13.class, args);
   }
}
