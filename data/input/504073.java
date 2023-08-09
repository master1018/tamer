public final class hc_characterdatasubstringexceedsvalue extends DOMTestCase {
   public hc_characterdatasubstringexceedsvalue(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node nameNode;
      CharacterData child;
      String substring;
      doc = (Document) load("hc_staff", false);
      elementList = doc.getElementsByTagName("strong");
      nameNode = elementList.item(0);
      child = (CharacterData) nameNode.getFirstChild();
      substring = child.substringData(9, 10);
      assertEquals("characterdataSubStringExceedsValueAssert", "Martin", substring);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_characterdatasubstringexceedsvalue.class, args);
   }
}
