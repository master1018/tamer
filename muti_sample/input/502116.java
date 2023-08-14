public final class nodeissupported05 extends DOMTestCase {
   public nodeissupported05(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      ProcessingInstruction pi;
      boolean success;
      doc = (Document) load("staffNS", false);
      pi = doc.createProcessingInstruction("PITarget", "PIData");
      success = pi.isSupported("-", "+");
      assertFalse("nodeissupported05", success);
}
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodeissupported05.class, args);
   }
}
