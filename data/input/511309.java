public final class namednodemapsetnameditemwithnewvalue extends DOMTestCase {
   public namednodemapsetnameditemwithnewvalue(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Attr newAttribute;
      Node testAddress;
      NamedNodeMap attributes;
      Node newNode;
      doc = (Document) load("staff", true);
      elementList = doc.getElementsByTagName("address");
      testAddress = elementList.item(2);
      newAttribute = doc.createAttribute("district");
      attributes = testAddress.getAttributes();
      newNode = attributes.setNamedItem(newAttribute);
      assertNull("returnedNodeNull", newNode);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(namednodemapsetnameditemwithnewvalue.class, args);
   }
}
