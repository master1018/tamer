public final class nodeinsertbeforenomodificationallowederrEE extends DOMTestCase {
   public nodeinsertbeforenomodificationallowederrEE(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      Node entRef;
      Node createdNode;
      Node insertedNode;
      Node refChild = null;
      doc = (Document) load("staff", true);
      entRef = doc.createEntityReference("ent4");
      assertNotNull("createdEntRefNotNull", entRef);
      createdNode = doc.createElement("text3");
      {
         boolean success = false;
         try {
            insertedNode = entRef.insertBefore(createdNode, refChild);
          } catch (DOMException ex) {
            success = (ex.code == DOMException.NO_MODIFICATION_ALLOWED_ERR);
         }
         assertTrue("throw_NO_MODIFICATION_ALLOWED_ERR", success);
      }
}
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodeinsertbeforenomodificationallowederrEE.class, args);
   }
}
