public final class nodeprocessinginstructionnodevalue extends DOMTestCase {
   public nodeprocessinginstructionnodevalue(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList testList;
      Node piNode;
      String piValue;
      doc = (Document) load("staff", false);
      testList = doc.getChildNodes();
      piNode = testList.item(0);
      piValue = piNode.getNodeValue();
      assertEquals("value", "PIDATA", piValue);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodeprocessinginstructionnodevalue.class, args);
   }
}
