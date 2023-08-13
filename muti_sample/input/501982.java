public final class localName04 extends DOMTestCase {
   public localName04(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      org.w3c.domts.DocumentBuilderSetting[] settings = 
          new org.w3c.domts.DocumentBuilderSetting[] {
org.w3c.domts.DocumentBuilderSetting.namespaceAware
        };
        DOMTestDocumentBuilderFactory testFactory = factory.newInstance(settings);
        setFactory(testFactory);
    String contentType = getContentType();
    preload(contentType, "staffNS", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node testEmployee;
      String employeeLocalName;
      doc = (Document) load("staffNS", false);
      elementList = doc.getElementsByTagName("employee");
      testEmployee = elementList.item(0);
      employeeLocalName = testEmployee.getLocalName();
      assertEquals("lname", "employee", employeeLocalName);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(localName04.class, args);
   }
}
