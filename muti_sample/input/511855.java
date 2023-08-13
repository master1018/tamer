public final class hc_nodevalue02 extends DOMTestCase {
   public hc_nodevalue02(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      Node newNode;
      String newValue;
      doc = (Document) load("hc_staff", true);
      newNode = doc.createComment("This is a new Comment node");
      newValue = newNode.getNodeValue();
      assertEquals("initial", "This is a new Comment node", newValue);
      newNode.setNodeValue("This should have an effect");
      newValue = newNode.getNodeValue();
      assertEquals("afterChange", "This should have an effect", newValue);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_nodevalue02.class, args);
   }
}
