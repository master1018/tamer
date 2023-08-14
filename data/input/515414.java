public final class hc_attrspecifiedvaluechanged extends DOMTestCase {
   public hc_attrspecifiedvaluechanged(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      boolean state;
      doc = (Document) load("hc_staff", true);
      addressList = doc.getElementsByTagName("acronym");
      testNode = addressList.item(2);
      ((Element) testNode).setAttribute("class", "Y\u03b1"); 
      attributes = testNode.getAttributes();
      streetAttr = (Attr) attributes.getNamedItem("class");
      state = streetAttr.getSpecified();
      assertTrue("acronymClassSpecified", state);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_attrspecifiedvaluechanged.class, args);
   }
}
