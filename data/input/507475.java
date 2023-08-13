public final class hc_nodeinsertbeforerefchildnonexistent extends DOMTestCase {
   public hc_nodeinsertbeforerefchildnonexistent(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      Node refChild;
      Node newChild;
      NodeList elementList;
      Node elementNode;
      Node insertedNode;
      doc = (Document) load("hc_staff", true);
      newChild = doc.createElement("br");
      refChild = doc.createElement("b");
      elementList = doc.getElementsByTagName("p");
      elementNode = elementList.item(1);
      {
         boolean success = false;
         try {
            insertedNode = elementNode.insertBefore(newChild, refChild);
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
        DOMTestCase.doMain(hc_nodeinsertbeforerefchildnonexistent.class, args);
   }
}
