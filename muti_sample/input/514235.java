public final class namednodemapsetnameditemreturnvalue extends DOMTestCase {
   public namednodemapsetnameditemreturnvalue(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      String attrValue;
      doc = (Document) load("staff", true);
      elementList = doc.getElementsByTagName("address");
      testAddress = elementList.item(2);
      newAttribute = doc.createAttribute("street");
      attributes = testAddress.getAttributes();
      newNode = attributes.setNamedItem(newAttribute);
      attrValue = newNode.getNodeValue();
      assertEquals("returnedNodeValue", "No", attrValue);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(namednodemapsetnameditemreturnvalue.class, args);
   }
}
