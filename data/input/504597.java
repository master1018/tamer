public final class hc_noderemovechildoldchildnonexistent extends DOMTestCase {
   public hc_noderemovechildoldchildnonexistent(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      Node oldChild;
      NodeList elementList;
      Node elementNode;
      Node removedChild;
      doc = (Document) load("hc_staff", true);
      oldChild = doc.createElement("br");
      elementList = doc.getElementsByTagName("p");
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
        DOMTestCase.doMain(hc_noderemovechildoldchildnonexistent.class, args);
   }
}
