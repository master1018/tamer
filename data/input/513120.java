public final class namednodemapgetnameditem extends DOMTestCase {
   public namednodemapgetnameditem(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node testEmployee;
      NamedNodeMap attributes;
      Attr domesticAttr;
      String attrName;
      doc = (Document) load("staff", false);
      elementList = doc.getElementsByTagName("address");
      testEmployee = elementList.item(1);
      attributes = testEmployee.getAttributes();
      domesticAttr = (Attr) attributes.getNamedItem("domestic");
      attrName = domesticAttr.getNodeName();
      assertEquals("namednodemapGetNamedItemAssert", "domestic", attrName);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(namednodemapgetnameditem.class, args);
   }
}
