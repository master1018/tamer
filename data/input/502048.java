public final class nodeissupported04 extends DOMTestCase {
   public nodeissupported04(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      EntityReference entRef;
      boolean success;
      doc = (Document) load("staffNS", false);
      entRef = doc.createEntityReference("ent1");
      assertNotNull("createdEntRefNotNull", entRef);
      success = entRef.isSupported("XML CORE", "");
      assertFalse("nodeissupported04", success);
}
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodeissupported04.class, args);
   }
}
