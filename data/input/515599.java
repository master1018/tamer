public final class attrnotspecifiedvalue extends DOMTestCase {
   public attrnotspecifiedvalue(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      org.w3c.domts.DocumentBuilderSetting[] settings = 
          new org.w3c.domts.DocumentBuilderSetting[] {
org.w3c.domts.DocumentBuilderSetting.validating
        };
        DOMTestDocumentBuilderFactory testFactory = factory.newInstance(settings);
        setFactory(testFactory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList addressList;
      Node testNode;
      NamedNodeMap attributes;
      Attr streetAttr;
      boolean state;
      doc = (Document) load("staff", false);
      addressList = doc.getElementsByTagName("address");
      testNode = addressList.item(0);
      attributes = testNode.getAttributes();
      streetAttr = (Attr) attributes.getNamedItem("street");
      state = streetAttr.getSpecified();
      assertFalse("streetNotSpecified", state);
}
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(attrnotspecifiedvalue.class, args);
   }
}
