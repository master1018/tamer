public final class nodevalue05 extends DOMTestCase {
   public nodevalue05(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      Node newNode;
      String newValue;
      doc = (Document) load("staff", true);
      newNode = doc.createDocumentFragment();
      newValue = newNode.getNodeValue();
      assertNull("initiallyNull", newValue);
      newNode.setNodeValue("This should have no effect");
      newValue = newNode.getNodeValue();
      assertNull("nullAfterAttemptedChange", newValue);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodevalue05.class, args);
   }
}
