public final class hc_namednodemapsetnameditemreturnvalue extends DOMTestCase {
   public hc_namednodemapsetnameditemreturnvalue(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      String attrValue;
      doc = (Document) load("hc_staff", true);
      elementList = doc.getElementsByTagName("acronym");
      testAddress = elementList.item(2);
      newAttribute = doc.createAttribute("class");
      attributes = testAddress.getAttributes();
      newNode = attributes.setNamedItem(newAttribute);
      assertNotNull("previousAttrNotNull", newNode);
      attrValue = newNode.getNodeValue();
      assertEquals("previousAttrValue", "No", attrValue);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_namednodemapsetnameditemreturnvalue.class, args);
   }
}
