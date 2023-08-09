public final class attrname extends DOMTestCase {
   public attrname(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList addressList;
      Node testNode;
      NamedNodeMap attributes;
      Attr streetAttr;
      String name;
      doc = (Document) load("staff", false);
      addressList = doc.getElementsByTagName("address");
      testNode = addressList.item(1);
      attributes = testNode.getAttributes();
      streetAttr = (Attr) attributes.getNamedItem("street");
      name = streetAttr.getNodeName();
      assertEquals("nodeName", "street", name);
      name = streetAttr.getName();
      assertEquals("name", "street", name);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(attrname.class, args);
   }
}
