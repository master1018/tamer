public final class hc_nodeinsertbeforenewchilddiffdocument extends DOMTestCase {
   public hc_nodeinsertbeforenewchilddiffdocument(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", false);
    preload(contentType, "hc_staff", true);
    }
   public void runTest() throws Throwable {
      Document doc1;
      Document doc2;
      Node refChild;
      Node newChild;
      NodeList elementList;
      Node elementNode;
      Node insertedNode;
      doc1 = (Document) load("hc_staff", false);
      doc2 = (Document) load("hc_staff", true);
      newChild = doc1.createElement("br");
      elementList = doc2.getElementsByTagName("p");
      elementNode = elementList.item(1);
      refChild = elementNode.getFirstChild();
      {
         boolean success = false;
         try {
            insertedNode = elementNode.insertBefore(newChild, refChild);
          } catch (DOMException ex) {
            success = (ex.code == DOMException.WRONG_DOCUMENT_ERR);
         }
         assertTrue("throw_WRONG_DOCUMENT_ERR", success);
      }
}
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_nodeinsertbeforenewchilddiffdocument.class, args);
   }
}
