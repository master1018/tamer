public final class hc_documentgetimplementation extends DOMTestCase {
   public hc_documentgetimplementation(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      DOMImplementation docImpl;
      boolean xmlstate;
      boolean htmlstate;
      doc = (Document) load("hc_staff", false);
      docImpl = doc.getImplementation();
      xmlstate = docImpl.hasFeature("XML", "1.0");
htmlstate = docImpl.hasFeature("HTML", "1.0");
      if (("text/html".equals(getContentType()))) {
          assertTrue("supports_HTML_1.0", htmlstate);
      } else {
          assertTrue("supports_XML_1.0", xmlstate);
      }
    }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_documentgetimplementation.class, args);
   }
}
