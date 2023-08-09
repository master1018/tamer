public final class prefix02 extends DOMTestCase {
   public prefix02(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node testEmployee;
      Node textNode;
      String prefix;
      doc = (Document) load("staffNS", false);
      elementList = doc.getElementsByTagName("emp:employeeId");
      testEmployee = elementList.item(0);
      assertNotNull("empEmployeeNotNull", testEmployee);
      textNode = testEmployee.getFirstChild();
      prefix = textNode.getPrefix();
      assertNull("textNodePrefix", prefix);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(prefix02.class, args);
   }
}
