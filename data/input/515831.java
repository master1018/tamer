public final class hc_attrname extends DOMTestCase {
   public hc_attrname(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList addressList;
      Node testNode;
      NamedNodeMap attributes;
      Attr streetAttr;
      String strong1;
      String strong2;
      doc = (Document) load("hc_staff", false);
      addressList = doc.getElementsByTagName("acronym");
      testNode = addressList.item(1);
      attributes = testNode.getAttributes();
      streetAttr = (Attr) attributes.getNamedItem("class");
      strong1 = streetAttr.getNodeName();
      strong2 = streetAttr.getName();
      assertEqualsAutoCase("attribute", "nodeName", "class", strong1);
        assertEqualsAutoCase("attribute", "name", "class", strong2);
        }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_attrname.class, args);
   }
}
