public final class nodeelementnodevalue extends DOMTestCase {
   public nodeelementnodevalue(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      Element elementNode;
      String elementValue;
      doc = (Document) load("staff", false);
      elementNode = doc.getDocumentElement();
      elementValue = elementNode.getNodeValue();
      assertNull("elementNodeValueNull", elementValue);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodeelementnodevalue.class, args);
   }
}
