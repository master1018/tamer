public final class elementremoveattribute extends DOMTestCase {
   public elementremoveattribute(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      NodeList elementList;
      Element testEmployee;
      String attrValue;
      doc = (Document) load("staff", true);
      elementList = doc.getElementsByTagName("address");
      testEmployee = (Element) elementList.item(3);
      testEmployee.removeAttribute("street");
      attrValue = testEmployee.getAttribute("street");
      assertEquals("streetYes", "Yes", attrValue);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(elementremoveattribute.class, args);
   }
}
