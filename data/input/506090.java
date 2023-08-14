public final class hc_domimplementationfeaturenoversion extends DOMTestCase {
   public hc_domimplementationfeaturenoversion(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
          state = domImpl.hasFeature("HTML", "");
} else {
          state = domImpl.hasFeature("XML", "");
}
    assertTrue("hasFeatureBlank", state);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_domimplementationfeaturenoversion.class, args);
   }
}
