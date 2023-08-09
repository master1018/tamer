public final class hc_characterdatadeletedatamiddle extends DOMTestCase {
   public hc_characterdatadeletedatamiddle(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      elementList = doc.getElementsByTagName("acronym");
      nameNode = elementList.item(0);
      child = (CharacterData) nameNode.getFirstChild();
      child.deleteData(16, 8);
      childData = child.getData();
      assertEquals("characterdataDeleteDataMiddleAssert", "1230 North Ave. Texas 98551", childData);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_characterdatadeletedatamiddle.class, args);
   }
}
