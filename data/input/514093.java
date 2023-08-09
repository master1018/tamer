public final class characterdataindexsizeerrdeletedataoffsetgreater extends DOMTestCase {
   public characterdataindexsizeerrdeletedataoffsetgreater(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node nameNode;
      CharacterData child;
      doc = (Document) load("staff", true);
      elementList = doc.getElementsByTagName("address");
      nameNode = elementList.item(0);
      child = (CharacterData) nameNode.getFirstChild();
      {
         boolean success = false;
         try {
            child.deleteData(40, 3);
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
        DOMTestCase.doMain(characterdataindexsizeerrdeletedataoffsetgreater.class, args);
   }
}
