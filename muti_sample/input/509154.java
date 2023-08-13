public final class characterdatareplacedataend extends DOMTestCase {
   public characterdatareplacedataend(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      elementList = doc.getElementsByTagName("address");
      nameNode = elementList.item(0);
      child = (CharacterData) nameNode.getFirstChild();
      child.replaceData(30, 5, "98665");
      childData = child.getData();
      assertEquals("characterdataReplaceDataEndAssert", "1230 North Ave. Dallas, Texas 98665", childData);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(characterdatareplacedataend.class, args);
   }
}
