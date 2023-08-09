public final class nodesetnodevaluenomodificationallowederrEE extends DOMTestCase {
   public nodesetnodevaluenomodificationallowederrEE(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      EntityReference entRef;
      CharacterData entText;
      doc = (Document) load("staff", true);
      entRef = doc.createEntityReference("ent3");
      assertNotNull("createdEntRefNotNull", entRef);
      entText = (CharacterData) entRef.getFirstChild();
      assertNotNull("entTextNotNull", entText);
      {
         boolean success = false;
         try {
            entText.setNodeValue("newValue");
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
        DOMTestCase.doMain(nodesetnodevaluenomodificationallowederrEE.class, args);
   }
}
