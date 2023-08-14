public final class documentgetelementsbytagnameNS04 extends DOMTestCase {
   public documentgetelementsbytagnameNS04(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      NodeList childList;
      String nullNS = null;
      doc = (Document) load("staffNS", false);
      childList = doc.getElementsByTagNameNS(nullNS, "0");
      assertSize("documentgetelementsbytagnameNS04", 0, childList);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(documentgetelementsbytagnameNS04.class, args);
   }
}
