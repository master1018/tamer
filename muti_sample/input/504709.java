public final class characterdatadeletedatabegining extends DOMTestCase {
   public characterdatadeletedatabegining(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      child.deleteData(0, 16);
      childData = child.getData();
      assertEquals("characterdataDeleteDataBeginingAssert", "Dallas, Texas 98551", childData);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(characterdatadeletedatabegining.class, args);
   }
}
