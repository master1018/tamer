public final class hc_characterdatadeletedatabegining extends DOMTestCase {
   public hc_characterdatadeletedatabegining(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      child.deleteData(0, 16);
      childData = child.getData();
      assertEquals("data", "Dallas, Texas 98551", childData);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_characterdatadeletedatabegining.class, args);
   }
}
