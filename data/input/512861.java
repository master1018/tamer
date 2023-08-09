public final class hc_characterdatasetnodevalue extends DOMTestCase {
   public hc_characterdatasetnodevalue(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node nameNode;
      CharacterData child;
      String childData;
      String childValue;
      doc = (Document) load("hc_staff", true);
      elementList = doc.getElementsByTagName("strong");
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
        DOMTestCase.doMain(hc_characterdatasetnodevalue.class, args);
   }
}
