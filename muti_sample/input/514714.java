public final class namednodemapreturnattrnode extends DOMTestCase {
   public namednodemapreturnattrnode(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node testEmployee;
      NamedNodeMap attributes;
      Node streetAttr;
      String attrName;
      doc = (Document) load("staff", false);
      elementList = doc.getElementsByTagName("address");
      testEmployee = elementList.item(1);
      attributes = testEmployee.getAttributes();
      streetAttr = attributes.getNamedItem("street");
      assertInstanceOf("typeAssert", Attr.class, streetAttr);
attrName = streetAttr.getNodeName();
      assertEquals("nodeName", "street", attrName);
      attrName = ((Attr) streetAttr).getName();
      assertEquals("attrName", "street", attrName);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(namednodemapreturnattrnode.class, args);
   }
}
