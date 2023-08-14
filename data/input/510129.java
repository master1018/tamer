public final class hc_attrcreatetextnode2 extends DOMTestCase {
   public hc_attrcreatetextnode2(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList addressList;
      Node testNode;
      NamedNodeMap attributes;
      Attr streetAttr;
      String value;
      doc = (Document) load("hc_staff", true);
      addressList = doc.getElementsByTagName("acronym");
      testNode = addressList.item(3);
      attributes = testNode.getAttributes();
      streetAttr = (Attr) attributes.getNamedItem("class");
      streetAttr.setNodeValue("Y&ent1;");
      value = streetAttr.getValue();
      assertEquals("value", "Y&ent1;", value);
      value = streetAttr.getNodeValue();
      assertEquals("nodeValue", "Y&ent1;", value);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_attrcreatetextnode2.class, args);
   }
}
