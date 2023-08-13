public final class attrcreatetextnode extends DOMTestCase {
   public attrcreatetextnode(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList addressList;
      Node testNode;
      NamedNodeMap attributes;
      Attr streetAttr;
      String value;
      doc = (Document) load("staff", true);
      addressList = doc.getElementsByTagName("address");
      testNode = addressList.item(3);
      attributes = testNode.getAttributes();
      streetAttr = (Attr) attributes.getNamedItem("street");
      streetAttr.setValue("Y&ent1;");
      value = streetAttr.getValue();
      assertEquals("value", "Y&ent1;", value);
      value = streetAttr.getNodeValue();
      assertEquals("nodeValue", "Y&ent1;", value);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(attrcreatetextnode.class, args);
   }
}
