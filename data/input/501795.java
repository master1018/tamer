public final class processinginstructiongettarget extends DOMTestCase {
   public processinginstructiongettarget(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList childNodes;
      ProcessingInstruction piNode;
      String target;
      doc = (Document) load("staff", false);
      childNodes = doc.getChildNodes();
      piNode = (ProcessingInstruction) childNodes.item(0);
      target = piNode.getTarget();
      assertEquals("processinginstructionGetTargetAssert", "TEST-STYLE", target);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(processinginstructiongettarget.class, args);
   }
}
