public final class processinginstructiongetdata extends DOMTestCase {
   public processinginstructiongetdata(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList childNodes;
      ProcessingInstruction piNode;
      String data;
      doc = (Document) load("staff", false);
      childNodes = doc.getChildNodes();
      piNode = (ProcessingInstruction) childNodes.item(0);
      data = piNode.getData();
      assertEquals("processinginstructionGetTargetAssert", "PIDATA", data);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(processinginstructiongetdata.class, args);
   }
}
