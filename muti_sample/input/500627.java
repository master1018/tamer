public final class nodecommentnodevalue extends DOMTestCase {
   public nodecommentnodevalue(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node commentNode;
      String commentName;
      String commentValue;
      doc = (Document) load("staff", false);
      elementList = doc.getChildNodes();
      for (int indexN10040 = 0; indexN10040 < elementList.getLength(); indexN10040++) {
          commentNode = (Node) elementList.item(indexN10040);
    commentName = commentNode.getNodeName();
      if (equals("#comment", commentName)) {
          commentValue = commentNode.getNodeValue();
      assertEquals("value", " This is comment number 1.", commentValue);
      }
      }
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodecommentnodevalue.class, args);
   }
}
