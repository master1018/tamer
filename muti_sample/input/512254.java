public final class attrspecifiedvaluechanged extends DOMTestCase {
   public attrspecifiedvaluechanged(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      boolean state;
      doc = (Document) load("staff", true);
      addressList = doc.getElementsByTagName("address");
      testNode = addressList.item(2);
      ((Element) testNode).setAttribute("street", "Yes");
      attributes = testNode.getAttributes();
      streetAttr = (Attr) attributes.getNamedItem("street");
      state = streetAttr.getSpecified();
      assertTrue("streetSpecified", state);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(attrspecifiedvaluechanged.class, args);
   }
}
