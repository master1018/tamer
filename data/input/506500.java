public final class elementremoveattributerestoredefaultvalue extends DOMTestCase {
   public elementremoveattributerestoredefaultvalue(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      Attr streetAttr;
      String attribute;
      Attr removedAttr;
      doc = (Document) load("staff", true);
      elementList = doc.getElementsByTagName("address");
      testEmployee = (Element) elementList.item(2);
      streetAttr = testEmployee.getAttributeNode("street");
      removedAttr = testEmployee.removeAttributeNode(streetAttr);
      attribute = testEmployee.getAttribute("street");
      assertEquals("streetYes", "Yes", attribute);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(elementremoveattributerestoredefaultvalue.class, args);
   }
}
