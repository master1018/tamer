public final class characterdatareplacedataexceedslengthofarg extends DOMTestCase {
   public characterdatareplacedataexceedslengthofarg(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      child.replaceData(0, 4, "260030");
      childData = child.getData();
      assertEquals("characterdataReplaceDataExceedsLengthOfArgAssert", "260030 North Ave. Dallas, Texas 98551", childData);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(characterdatareplacedataexceedslengthofarg.class, args);
   }
}
