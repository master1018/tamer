public final class namednodemapsetnameditem extends DOMTestCase {
   public namednodemapsetnameditem(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      Attr districtNode;
      String attrName;
      Node setNode;
      doc = (Document) load("staff", true);
      elementList = doc.getElementsByTagName("address");
      testAddress = elementList.item(1);
      newAttribute = doc.createAttribute("district");
      attributes = testAddress.getAttributes();
      setNode = attributes.setNamedItem(newAttribute);
      districtNode = (Attr) attributes.getNamedItem("district");
      attrName = districtNode.getNodeName();
      assertEquals("namednodemapSetNamedItemAssert", "district", attrName);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(namednodemapsetnameditem.class, args);
   }
}
