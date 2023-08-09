public final class hc_namednodemapgetnameditem extends DOMTestCase {
   public hc_namednodemapgetnameditem(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node testEmployee;
      NamedNodeMap attributes;
      Attr domesticAttr;
      String attrName;
      doc = (Document) load("hc_staff", false);
      elementList = doc.getElementsByTagName("acronym");
      testEmployee = elementList.item(1);
      attributes = testEmployee.getAttributes();
      domesticAttr = (Attr) attributes.getNamedItem("title");
      attrName = domesticAttr.getNodeName();
      assertEqualsAutoCase("attribute", "nodeName", "title", attrName);
        }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_namednodemapgetnameditem.class, args);
   }
}
