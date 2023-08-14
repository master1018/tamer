public final class characterdatainsertdatabeginning extends DOMTestCase {
   public characterdatainsertdatabeginning(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node nameNode;
      CharacterData child;
      String childData;
      doc = (Document) load("staff", true);
      elementList = doc.getElementsByTagName("name");
      nameNode = elementList.item(0);
      child = (CharacterData) nameNode.getFirstChild();
      child.insertData(0, "Mss. ");
      childData = child.getData();
      assertEquals("characterdataInsertDataBeginningAssert", "Mss. Margaret Martin", childData);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(characterdatainsertdatabeginning.class, args);
   }
}
