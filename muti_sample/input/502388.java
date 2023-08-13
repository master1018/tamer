public final class hc_nodereplacechildinvalidnodetype extends DOMTestCase {
   public hc_nodereplacechildinvalidnodetype(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      Element rootNode;
      Node newChild;
      NodeList elementList;
      Node oldChild;
      Node replacedChild;
      doc = (Document) load("hc_staff", true);
      newChild = doc.createAttribute("lang");
      elementList = doc.getElementsByTagName("p");
      oldChild = elementList.item(1);
      rootNode = (Element) oldChild.getParentNode();
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
        DOMTestCase.doMain(hc_nodereplacechildinvalidnodetype.class, args);
   }
}
