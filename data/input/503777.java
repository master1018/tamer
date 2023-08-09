public final class characterdatareplacedatamiddle extends DOMTestCase {
   public characterdatareplacedatamiddle(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      child.replaceData(5, 5, "South");
      childData = child.getData();
      assertEquals("characterdataReplaceDataMiddleAssert", "1230 South Ave. Dallas, Texas 98551", childData);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(characterdatareplacedatamiddle.class, args);
   }
}
