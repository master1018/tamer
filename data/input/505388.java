public final class attrspecifiedvalueremove extends DOMTestCase {
   public attrspecifiedvalueremove(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      org.w3c.domts.DocumentBuilderSetting[] settings = 
          new org.w3c.domts.DocumentBuilderSetting[] {
org.w3c.domts.DocumentBuilderSetting.validating
        };
        DOMTestDocumentBuilderFactory testFactory = factory.newInstance(settings);
        setFactory(testFactory);
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
      ((Element) testNode).removeAttribute("street");
      attributes = testNode.getAttributes();
      streetAttr = (Attr) attributes.getNamedItem("street");
      assertNotNull("streetAttrNotNull", streetAttr);
      state = streetAttr.getSpecified();
      assertFalse("attrSpecifiedValueRemoveAssert", state);
}
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(attrspecifiedvalueremove.class, args);
   }
}
