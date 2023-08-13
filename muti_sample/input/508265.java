public final class hc_nodevalue03 extends DOMTestCase {
   public hc_nodevalue03(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      Node newNode;
      String newValue;
      doc = (Document) load("hc_staff", true);
      if (("text/html".equals(getContentType()))) {
      {
         boolean success = false;
         try {
            newNode = doc.createEntityReference("ent1");
          } catch (DOMException ex) {
            success = (ex.code == DOMException.NOT_SUPPORTED_ERR);
         }
         assertTrue("throw_NOT_SUPPORTED_ERR", success);
      }
} else {
          newNode = doc.createEntityReference("ent1");
      assertNotNull("createdEntRefNotNull", newNode);
      newValue = newNode.getNodeValue();
      assertNull("initiallyNull", newValue);
      newNode.setNodeValue("This should have no effect");
      newValue = newNode.getNodeValue();
      assertNull("nullAfterAttemptedChange", newValue);
      }
    }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_nodevalue03.class, args);
   }
}
