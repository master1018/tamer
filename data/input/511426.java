public final class hc_nodedocumentfragmentnodename extends DOMTestCase {
   public hc_nodedocumentfragmentnodename(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      DocumentFragment docFragment;
      String documentFragmentName;
      doc = (Document) load("hc_staff", true);
      docFragment = doc.createDocumentFragment();
      documentFragmentName = docFragment.getNodeName();
      assertEquals("nodeDocumentFragmentNodeNameAssert1", "#document-fragment", documentFragmentName);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_nodedocumentfragmentnodename.class, args);
   }
}
