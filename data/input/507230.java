public final class localName03 extends DOMTestCase {
   public localName03(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node testEmployee;
      Node textNode;
      String localName;
      doc = (Document) load("staffNS", false);
      elementList = doc.getElementsByTagName("employeeId");
      testEmployee = elementList.item(0);
      textNode = testEmployee.getFirstChild();
      localName = textNode.getLocalName();
      assertNull("textNodeLocalName", localName);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(localName03.class, args);
   }
}
