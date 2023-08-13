public final class hc_attrprevioussiblingnull extends DOMTestCase {
   public hc_attrprevioussiblingnull(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList addressList;
      Node testNode;
      NamedNodeMap attributes;
      Attr domesticAttr;
      Node s;
      doc = (Document) load("hc_staff", false);
      addressList = doc.getElementsByTagName("acronym");
      testNode = addressList.item(0);
      attributes = testNode.getAttributes();
      domesticAttr = (Attr) attributes.getNamedItem("title");
      s = domesticAttr.getPreviousSibling();
      assertNull("attrPreviousSiblingNullAssert", s);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_attrprevioussiblingnull.class, args);
   }
}
