public final class hc_nodeparentnodenull extends DOMTestCase {
   public hc_nodeparentnodenull(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      Element createdNode;
      Node parentNode;
      doc = (Document) load("hc_staff", false);
      createdNode = doc.createElement("br");
      parentNode = createdNode.getParentNode();
      assertNull("parentNode", parentNode);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_nodeparentnodenull.class, args);
   }
}
