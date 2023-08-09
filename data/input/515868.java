public final class domimplementationfeaturenull extends DOMTestCase {
   public domimplementationfeaturenull(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      org.w3c.domts.DocumentBuilderSetting[] settings = 
          new org.w3c.domts.DocumentBuilderSetting[] {
org.w3c.domts.DocumentBuilderSetting.hasNullString
        };
        DOMTestDocumentBuilderFactory testFactory = factory.newInstance(settings);
        setFactory(testFactory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      DOMImplementation domImpl;
      boolean state;
      String nullVersion = null;
      doc = (Document) load("staff", false);
      domImpl = doc.getImplementation();
      state = domImpl.hasFeature("XML", nullVersion);
assertTrue("hasXMLnull", state);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(domimplementationfeaturenull.class, args);
   }
}
