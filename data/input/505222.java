public final class nodedocumentfragmentnodename extends DOMTestCase {
   public nodedocumentfragmentnodename(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      DocumentFragment docFragment;
      String documentFragmentName;
      doc = (Document) load("staff", true);
      docFragment = doc.createDocumentFragment();
      documentFragmentName = docFragment.getNodeName();
      assertEquals("nodeDocumentFragmentNodeNameAssert1", "#document-fragment", documentFragmentName);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodedocumentfragmentnodename.class, args);
   }
}
