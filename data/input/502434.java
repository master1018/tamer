public final class hc_domimplementationfeaturexml extends DOMTestCase {
   public hc_domimplementationfeaturexml(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
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
          state = domImpl.hasFeature("html", "1.0");
assertTrue("supports_html_1.0", state);
      } else {
          state = domImpl.hasFeature("xml", "1.0");
assertTrue("supports_xml_1.0", state);
      }
    }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_domimplementationfeaturexml.class, args);
   }
}
