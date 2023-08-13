public final class characterdatagetdata extends DOMTestCase {
   public characterdatagetdata(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node nameNode;
      CharacterData child;
      String childData;
      doc = (Document) load("staff", false);
      elementList = doc.getElementsByTagName("name");
      nameNode = elementList.item(0);
      child = (CharacterData) nameNode.getFirstChild();
      childData = child.getData();
      assertEquals("characterdataGetDataAssert", "Margaret Martin", childData);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(characterdatagetdata.class, args);
   }
}
