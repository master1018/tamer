public final class hc_noderemovechild extends DOMTestCase {
   public hc_noderemovechild(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      Element rootNode;
      NodeList childList;
      Node childToRemove;
      Node removedChild;
      Node parentNode;
      doc = (Document) load("hc_staff", true);
      rootNode = doc.getDocumentElement();
      childList = rootNode.getChildNodes();
      childToRemove = childList.item(1);
      removedChild = rootNode.removeChild(childToRemove);
      parentNode = removedChild.getParentNode();
      assertNull("parentNodeNull", parentNode);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_noderemovechild.class, args);
   }
}
