public final class characterdatasetnodevalue extends DOMTestCase {
   public characterdatasetnodevalue(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      String childValue;
      doc = (Document) load("staff", true);
      elementList = doc.getElementsByTagName("name");
      nameNode = elementList.item(0);
      child = (CharacterData) nameNode.getFirstChild();
      child.setNodeValue("Marilyn Martin");
      childData = child.getData();
      assertEquals("data", "Marilyn Martin", childData);
      childValue = child.getNodeValue();
      assertEquals("value", "Marilyn Martin", childValue);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(characterdatasetnodevalue.class, args);
   }
}
