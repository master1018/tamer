public final class characterdatadeletedatagetlengthanddata extends DOMTestCase {
   public characterdatadeletedatagetlengthanddata(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      int childLength;
      java.util.List result = new java.util.ArrayList();
      doc = (Document) load("staff", true);
      elementList = doc.getElementsByTagName("address");
      nameNode = elementList.item(0);
      child = (CharacterData) nameNode.getFirstChild();
      child.deleteData(30, 5);
      childData = child.getData();
      assertEquals("data", "1230 North Ave. Dallas, Texas ", childData);
      childLength = (int) child.getLength();
      assertEquals("length", 30, childLength);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(characterdatadeletedatagetlengthanddata.class, args);
   }
}
