public final class nodereplacechildinvalidnodetype extends DOMTestCase {
   public nodereplacechildinvalidnodetype(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      Element rootNode;
      Node newChild;
      NodeList elementList;
      Node oldChild;
      Node replacedChild;
      doc = (Document) load("staff", true);
      rootNode = doc.getDocumentElement();
      newChild = doc.createAttribute("newAttribute");
      elementList = doc.getElementsByTagName("employee");
      oldChild = elementList.item(1);
      {
         boolean success = false;
         try {
            replacedChild = rootNode.replaceChild(newChild, oldChild);
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
        DOMTestCase.doMain(nodereplacechildinvalidnodetype.class, args);
   }
}
