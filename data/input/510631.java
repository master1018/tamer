public final class prefix03 extends DOMTestCase {
   public prefix03(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      String prefix;
      doc = (Document) load("staffNS", false);
      elementList = doc.getElementsByTagName("emp:employee");
      testEmployee = elementList.item(0);
      assertNotNull("empEmployeeNotNull", testEmployee);
      prefix = testEmployee.getPrefix();
      assertEquals("prefix", "emp", prefix);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(prefix03.class, args);
   }
}
