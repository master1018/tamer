public final class nodeprocessinginstructionsetnodevalue extends DOMTestCase {
   public nodeprocessinginstructionsetnodevalue(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList testList;
      Node piNode;
      String piValue;
      doc = (Document) load("staff", true);
      testList = doc.getChildNodes();
      piNode = testList.item(0);
      piNode.setNodeValue("Something different");
      piValue = piNode.getNodeValue();
      assertEquals("nodeValue", "Something different", piValue);
      piValue = ((ProcessingInstruction) piNode).getData();
      assertEquals("data", "Something different", piValue);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodeprocessinginstructionsetnodevalue.class, args);
   }
}
