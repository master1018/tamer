public final class hc_characterdatagetlength extends DOMTestCase {
   public hc_characterdatagetlength(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node nameNode;
      CharacterData child;
      String childValue;
      int childLength;
      doc = (Document) load("hc_staff", false);
      elementList = doc.getElementsByTagName("strong");
      nameNode = elementList.item(0);
      child = (CharacterData) nameNode.getFirstChild();
      childValue = child.getData();
      childLength = childValue.length();
      assertEquals("characterdataGetLengthAssert", 15, childLength);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_characterdatagetlength.class, args);
   }
}
