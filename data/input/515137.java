public final class commentgetcomment extends DOMTestCase {
   public commentgetcomment(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node child;
      String childName;
      String childValue;
      int commentCount = 0;
      int childType;
      doc = (Document) load("staff", false);
      elementList = doc.getChildNodes();
      for (int indexN10057 = 0; indexN10057 < elementList.getLength(); indexN10057++) {
          child = (Node) elementList.item(indexN10057);
    childType = (int) child.getNodeType();
      if (equals(8, childType)) {
          childName = child.getNodeName();
      assertEquals("nodeName", "#comment", childName);
      childValue = child.getNodeValue();
      assertEquals("nodeValue", " This is comment number 1.", childValue);
      commentCount = commentCount + 1;
      }
      }
      assertEquals("commentCount", 1, commentCount);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(commentgetcomment.class, args);
   }
}
