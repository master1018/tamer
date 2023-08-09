public final class elementchangeattributevalue extends DOMTestCase {
   public elementchangeattributevalue(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
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
      testEmployee.setAttribute("street", "Neither");
      attrValue = testEmployee.getAttribute("street");
      assertEquals("elementChangeAttributeValueAssert", "Neither", attrValue);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(elementchangeattributevalue.class, args);
   }
}
