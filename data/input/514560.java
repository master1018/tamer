public final class hc_namednodemapsetnameditemthatexists extends DOMTestCase {
   public hc_namednodemapsetnameditemthatexists(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      Attr districtNode;
      String attrValue;
      Node setNode;
      doc = (Document) load("hc_staff", true);
      elementList = doc.getElementsByTagName("acronym");
      testAddress = elementList.item(1);
      newAttribute = doc.createAttribute("class");
      attributes = testAddress.getAttributes();
      setNode = attributes.setNamedItem(newAttribute);
      districtNode = (Attr) attributes.getNamedItem("class");
      attrValue = districtNode.getNodeValue();
      assertEquals("namednodemapSetNamedItemThatExistsAssert", "", attrValue);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_namednodemapsetnameditemthatexists.class, args);
   }
}
