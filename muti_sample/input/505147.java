public final class documentgetdoctypenodtd extends DOMTestCase {
   public documentgetdoctypenodtd(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      org.w3c.domts.DocumentBuilderSetting[] settings = 
          new org.w3c.domts.DocumentBuilderSetting[] {
org.w3c.domts.DocumentBuilderSetting.notValidating
        };
        DOMTestDocumentBuilderFactory testFactory = factory.newInstance(settings);
        setFactory(testFactory);
    String contentType = getContentType();
    preload(contentType, "hc_nodtdstaff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      DocumentType docType;
      doc = (Document) load("hc_nodtdstaff", false);
      docType = doc.getDoctype();
      assertNull("documentGetDocTypeNoDTDAssert", docType);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(documentgetdoctypenodtd.class, args);
   }
}
