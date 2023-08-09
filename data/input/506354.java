public final class namespaceURI03 extends DOMTestCase {
   public namespaceURI03(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      String employeeNamespace;
      doc = (Document) load("staffNS", false);
      elementList = doc.getElementsByTagName("employee");
      testEmployee = elementList.item(0);
      assertNotNull("employeeNotNull", testEmployee);
      employeeNamespace = testEmployee.getNamespaceURI();
      assertEquals("namespaceURI", "http:
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(namespaceURI03.class, args);
   }
}
