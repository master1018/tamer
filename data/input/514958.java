public final class nodeprocessinginstructionnodename extends DOMTestCase {
   public nodeprocessinginstructionnodename(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList testList;
      Node piNode;
      String piName;
      doc = (Document) load("staff", false);
      testList = doc.getChildNodes();
      piNode = testList.item(0);
      piName = piNode.getNodeName();
      assertEquals("nodeProcessingInstructionNodeNameAssert1", "TEST-STYLE", piName);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodeprocessinginstructionnodename.class, args);
   }
}
