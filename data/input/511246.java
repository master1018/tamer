public final class hc_nodeelementnodevalue extends DOMTestCase {
   public hc_nodeelementnodevalue(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      Element elementNode;
      String elementValue;
      doc = (Document) load("hc_staff", false);
      elementNode = doc.getDocumentElement();
      elementValue = elementNode.getNodeValue();
      assertNull("elementNodeValue", elementValue);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_nodeelementnodevalue.class, args);
   }
}
