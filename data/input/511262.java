public final class elementhasattribute02 extends DOMTestCase {
   public elementhasattribute02(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      org.w3c.domts.DocumentBuilderSetting[] settings = 
          new org.w3c.domts.DocumentBuilderSetting[] {
org.w3c.domts.DocumentBuilderSetting.validating
        };
        DOMTestDocumentBuilderFactory testFactory = factory.newInstance(settings);
        setFactory(testFactory);
    String contentType = getContentType();
    preload(contentType, "staffNS", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      Element element;
      boolean state;
      NodeList elementList;
      doc = (Document) load("staffNS", false);
      elementList = doc.getElementsByTagName("emp:employee");
      element = (Element) elementList.item(0);
      assertNotNull("empEmployeeNotNull", element);
      state = element.hasAttribute("defaultAttr");
      assertTrue("elementhasattribute02", state);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(elementhasattribute02.class, args);
   }
}
