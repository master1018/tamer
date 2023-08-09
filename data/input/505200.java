public final class hc_namednodemapreturnattrnode extends DOMTestCase {
   public hc_namednodemapreturnattrnode(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node testEmployee;
      NamedNodeMap attributes;
      Node streetAttr;
      String attrName;
      doc = (Document) load("hc_staff", false);
      elementList = doc.getElementsByTagName("acronym");
      testEmployee = elementList.item(1);
      attributes = testEmployee.getAttributes();
      streetAttr = attributes.getNamedItem("class");
      assertInstanceOf("typeAssert", Attr.class, streetAttr);
attrName = streetAttr.getNodeName();
      assertEqualsAutoCase("attribute", "nodeName", "class", attrName);
        attrName = ((Attr) streetAttr).getName();
      assertEqualsAutoCase("attribute", "name", "class", attrName);
        }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_namednodemapreturnattrnode.class, args);
   }
}
