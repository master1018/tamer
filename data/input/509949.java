public final class hc_nodereplacechildoldchildnonexistent extends DOMTestCase {
   public hc_nodereplacechildoldchildnonexistent(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      Node oldChild;
      Node newChild;
      NodeList elementList;
      Node elementNode;
      Node replacedNode;
      doc = (Document) load("hc_staff", true);
      newChild = doc.createElement("br");
      oldChild = doc.createElement("b");
      elementList = doc.getElementsByTagName("p");
      elementNode = elementList.item(1);
      {
         boolean success = false;
         try {
            replacedNode = elementNode.replaceChild(newChild, oldChild);
          } catch (DOMException ex) {
            success = (ex.code == DOMException.NOT_FOUND_ERR);
         }
         assertTrue("throw_NOT_FOUND_ERR", success);
      }
}
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_nodereplacechildoldchildnonexistent.class, args);
   }
}
