public final class namespaceURI04 extends DOMTestCase {
   public namespaceURI04(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
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
      testEmployee = elementList.item(1);
      employeeNamespace = testEmployee.getNamespaceURI();
      assertNull("throw_Null", employeeNamespace);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(namespaceURI04.class, args);
   }
}
