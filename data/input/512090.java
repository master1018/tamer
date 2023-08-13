public final class characterdataindexsizeerrsubstringoffsetgreater extends DOMTestCase {
   public characterdataindexsizeerrsubstringoffsetgreater(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node nameNode;
      CharacterData child;
      String badString;
      doc = (Document) load("staff", false);
      elementList = doc.getElementsByTagName("address");
      nameNode = elementList.item(0);
      child = (CharacterData) nameNode.getFirstChild();
      {
         boolean success = false;
         try {
            badString = child.substringData(40, 3);
          } catch (DOMException ex) {
            success = (ex.code == DOMException.INDEX_SIZE_ERR);
         }
         assertTrue("throw_INDEX_SIZE_ERR", success);
      }
}
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(characterdataindexsizeerrsubstringoffsetgreater.class, args);
   }
}
