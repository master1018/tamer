public final class nodevalue06 extends DOMTestCase {
   public nodevalue06(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document newNode;
      String newValue;
      newNode = (Document) load("staff", true);
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
        DOMTestCase.doMain(nodevalue06.class, args);
   }
}
