public final class hc_namednodemapsetnameditem extends DOMTestCase {
   public hc_namednodemapsetnameditem(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      String attrName;
      Node setNode;
      doc = (Document) load("hc_staff", true);
      elementList = doc.getElementsByTagName("acronym");
      testAddress = elementList.item(1);
      newAttribute = doc.createAttribute("lang");
      attributes = testAddress.getAttributes();
      setNode = attributes.setNamedItem(newAttribute);
      districtNode = (Attr) attributes.getNamedItem("lang");
      attrName = districtNode.getNodeName();
      assertEqualsAutoCase("attribute", "nodeName", "lang", attrName);
        }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_namednodemapsetnameditem.class, args);
   }
}
