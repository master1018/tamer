public final class elementretrieveallattributes extends DOMTestCase {
   public elementretrieveallattributes(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      Node testAddress;
      NamedNodeMap attributes;
      doc = (Document) load("staff", false);
      addressList = doc.getElementsByTagName("address");
      testAddress = addressList.item(0);
      attributes = testAddress.getAttributes();
      assertSize("elementRetrieveAllAttributesAssert", 2, attributes);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(elementretrieveallattributes.class, args);
   }
}
