public final class characterdatasubstringvalue extends DOMTestCase {
   public characterdatasubstringvalue(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node nameNode;
      CharacterData child;
      String substring;
      doc = (Document) load("staff", false);
      elementList = doc.getElementsByTagName("name");
      nameNode = elementList.item(0);
      child = (CharacterData) nameNode.getFirstChild();
      substring = child.substringData(0, 8);
      assertEquals("characterdataSubStringValueAssert", "Margaret", substring);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(characterdatasubstringvalue.class, args);
   }
}
