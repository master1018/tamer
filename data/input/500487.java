public final class nodeappendchildinvalidnodetype extends DOMTestCase {
   public nodeappendchildinvalidnodetype(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      Element rootNode;
      Node newChild;
      Node appendedChild;
      doc = (Document) load("staff", true);
      rootNode = doc.getDocumentElement();
      newChild = doc.createAttribute("newAttribute");
      {
         boolean success = false;
         try {
            appendedChild = rootNode.appendChild(newChild);
          } catch (DOMException ex) {
            success = (ex.code == DOMException.HIERARCHY_REQUEST_ERR);
         }
         assertTrue("throw_HIERARCHY_REQUEST_ERR", success);
      }
}
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodeappendchildinvalidnodetype.class, args);
   }
}
