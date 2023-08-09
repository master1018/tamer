public final class hasAttribute02 extends DOMTestCase {
   public hasAttribute02(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      NodeList elementList;
      Element testNode;
      boolean state;
      doc = (Document) load("staff", false);
      elementList = doc.getElementsByTagName("address");
      testNode = (Element) elementList.item(0);
      state = testNode.hasAttribute("street");
      assertTrue("throw_True", state);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hasAttribute02.class, args);
   }
}
