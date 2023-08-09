public final class hc_nodeappendchildinvalidnodetype extends DOMTestCase {
   public hc_nodeappendchildinvalidnodetype(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      Element rootNode;
      Node newChild;
      Node appendedChild;
      doc = (Document) load("hc_staff", true);
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
        DOMTestCase.doMain(hc_nodeappendchildinvalidnodetype.class, args);
   }
}
