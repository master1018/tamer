public final class noderemovechildoldchildnonexistent extends DOMTestCase {
   public noderemovechildoldchildnonexistent(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      Node oldChild;
      NodeList elementList;
      Node elementNode;
      Node removedChild;
      doc = (Document) load("staff", true);
      oldChild = doc.createElement("oldChild");
      elementList = doc.getElementsByTagName("employee");
      elementNode = elementList.item(1);
      {
         boolean success = false;
         try {
            removedChild = elementNode.removeChild(oldChild);
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
        DOMTestCase.doMain(noderemovechildoldchildnonexistent.class, args);
   }
}
