public final class hc_namednodemapsetnameditemwithnewvalue extends DOMTestCase {
   public hc_namednodemapsetnameditemwithnewvalue(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Attr newAttribute;
      Node testAddress;
      NamedNodeMap attributes;
      Node newNode;
      doc = (Document) load("hc_staff", true);
      elementList = doc.getElementsByTagName("acronym");
      testAddress = elementList.item(2);
      newAttribute = doc.createAttribute("lang");
      attributes = testAddress.getAttributes();
      newNode = attributes.setNamedItem(newAttribute);
      assertNull("prevValueNull", newNode);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_namednodemapsetnameditemwithnewvalue.class, args);
   }
}
