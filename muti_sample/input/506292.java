public final class nodevalue09 extends DOMTestCase {
   public nodevalue09(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      Node newNode;
      String newValue;
      doc = (Document) load("staff", true);
      newNode = doc.createProcessingInstruction("TARGET", "DATA");
      newValue = newNode.getNodeValue();
      assertEquals("initial", "DATA", newValue);
      newNode.setNodeValue("This should have an effect");
      newValue = newNode.getNodeValue();
      assertEquals("after", "This should have an effect", newValue);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodevalue09.class, args);
   }
}
