public final class noderemovechild extends DOMTestCase {
   public noderemovechild(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      Element rootNode;
      NodeList childList;
      Node childToRemove;
      Node removedChild;
      Node parentNode;
      doc = (Document) load("staff", true);
      rootNode = doc.getDocumentElement();
      childList = rootNode.getChildNodes();
      childToRemove = childList.item(1);
      removedChild = rootNode.removeChild(childToRemove);
      parentNode = removedChild.getParentNode();
      assertNull("nodeRemoveChildAssert1", parentNode);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(noderemovechild.class, args);
   }
}
