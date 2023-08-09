public final class documentcreateprocessinginstruction extends DOMTestCase {
   public documentcreateprocessinginstruction(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      ProcessingInstruction newPINode;
      String piValue;
      String piName;
      int piType;
      doc = (Document) load("staff", true);
      newPINode = doc.createProcessingInstruction("TESTPI", "This is a new PI node");
      assertNotNull("createdPINotNull", newPINode);
      piName = newPINode.getNodeName();
      assertEquals("name", "TESTPI", piName);
      piValue = newPINode.getNodeValue();
      assertEquals("value", "This is a new PI node", piValue);
      piType = (int) newPINode.getNodeType();
      assertEquals("type", 7, piType);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(documentcreateprocessinginstruction.class, args);
   }
}
