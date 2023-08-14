public final class namednodemapremovenameditemreturnnodevalue extends DOMTestCase {
   public namednodemapremovenameditemreturnnodevalue(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node testAddress;
      NamedNodeMap attributes;
      Node removedNode;
      String value;
      doc = (Document) load("staff", true);
      elementList = doc.getElementsByTagName("address");
      testAddress = elementList.item(2);
      attributes = testAddress.getAttributes();
      removedNode = attributes.removeNamedItem("street");
      value = removedNode.getNodeValue();
      assertEquals("namednodemapRemoveNamedItemReturnNodeValueAssert", "No", value);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(namednodemapremovenameditemreturnnodevalue.class, args);
   }
}
