public final class namednodemapreturnnull extends DOMTestCase {
   public namednodemapreturnnull(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node testEmployee;
      NamedNodeMap attributes;
      Attr districtNode;
      doc = (Document) load("staff", false);
      elementList = doc.getElementsByTagName("address");
      testEmployee = elementList.item(1);
      attributes = testEmployee.getAttributes();
      districtNode = (Attr) attributes.getNamedItem("district");
      assertNull("namednodemapReturnNullAssert", districtNode);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(namednodemapreturnnull.class, args);
   }
}
