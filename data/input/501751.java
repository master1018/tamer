public final class elementgetelementsbytagnamens02 extends DOMTestCase {
   public elementgetelementsbytagnamens02(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      NodeList elementList;
      doc = (Document) load("staffNS", false);
      element = doc.getDocumentElement();
      elementList = element.getElementsByTagNameNS("**", "*");
      assertSize("elementgetelementsbytagnamens02", 0, elementList);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(elementgetelementsbytagnamens02.class, args);
   }
}
