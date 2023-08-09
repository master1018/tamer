public final class hc_domimplementationfeaturenull extends DOMTestCase {
   public hc_domimplementationfeaturenull(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      org.w3c.domts.DocumentBuilderSetting[] settings = 
          new org.w3c.domts.DocumentBuilderSetting[] {
org.w3c.domts.DocumentBuilderSetting.hasNullString
        };
        DOMTestDocumentBuilderFactory testFactory = factory.newInstance(settings);
        setFactory(testFactory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      DOMImplementation domImpl;
      boolean state;
      doc = (Document) load("hc_staff", false);
      domImpl = doc.getImplementation();
      if (("text/html".equals(getContentType()))) {
          state = domImpl.hasFeature("HTML", null);
assertTrue("supports_HTML_null", state);
      } else {
          state = domImpl.hasFeature("XML", null);
assertTrue("supports_XML_null", state);
      }
    }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_domimplementationfeaturenull.class, args);
   }
}
