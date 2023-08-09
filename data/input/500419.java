public final class characterdataappenddata extends DOMTestCase {
   public characterdataappenddata(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node nameNode;
      CharacterData child;
      String childValue;
      int childLength;
      doc = (Document) load("staff", true);
      elementList = doc.getElementsByTagName("name");
      nameNode = elementList.item(0);
      child = (CharacterData) nameNode.getFirstChild();
      child.appendData(", Esquire");
      childValue = child.getData();
      childLength = childValue.length();
      assertEquals("characterdataAppendDataAssert", 24, childLength);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(characterdataappenddata.class, args);
   }
}
