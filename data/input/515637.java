public final class documentcreatecomment extends DOMTestCase {
   public documentcreatecomment(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      Comment newCommentNode;
      String newCommentValue;
      String newCommentName;
      int newCommentType;
      doc = (Document) load("staff", true);
      newCommentNode = doc.createComment("This is a new Comment node");
      newCommentValue = newCommentNode.getNodeValue();
      assertEquals("value", "This is a new Comment node", newCommentValue);
      newCommentName = newCommentNode.getNodeName();
      assertEquals("name", "#comment", newCommentName);
      newCommentType = (int) newCommentNode.getNodeType();
      assertEquals("type", 8, newCommentType);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(documentcreatecomment.class, args);
   }
}
