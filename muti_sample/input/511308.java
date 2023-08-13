public final class hc_characterdataappenddatagetdata extends DOMTestCase {
   public hc_characterdataappenddatagetdata(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      doc = (Document) load("hc_staff", true);
      elementList = doc.getElementsByTagName("strong");
      nameNode = elementList.item(0);
      child = (CharacterData) nameNode.getFirstChild();
      child.appendData(", Esquire");
      childData = child.getData();
      assertEquals("characterdataAppendDataGetDataAssert", "Margaret Martin, Esquire", childData);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_characterdataappenddatagetdata.class, args);
   }
}
