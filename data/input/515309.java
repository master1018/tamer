public final class nodeparentnodenull extends DOMTestCase {
   public nodeparentnodenull(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      Element createdNode;
      Node parentNode;
      doc = (Document) load("staff", false);
      createdNode = doc.createElement("employee");
      parentNode = createdNode.getParentNode();
      assertNull("parentNode", parentNode);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodeparentnodenull.class, args);
   }
}
