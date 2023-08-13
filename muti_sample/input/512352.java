public final class prefix04 extends DOMTestCase {
   public prefix04(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node testEmployee;
      String prefix;
      doc = (Document) load("staffNS", false);
      elementList = doc.getElementsByTagName("employee");
      testEmployee = elementList.item(0);
      prefix = testEmployee.getPrefix();
      assertNull("throw_Null", prefix);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(prefix04.class, args);
   }
}
