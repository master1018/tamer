public final class elementaddnewattribute extends DOMTestCase {
   public elementaddnewattribute(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      testEmployee = (Element) elementList.item(4);
      testEmployee.setAttribute("district", "dallas");
      attrValue = testEmployee.getAttribute("district");
      assertEquals("elementAddNewAttributeAssert", "dallas", attrValue);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(elementaddnewattribute.class, args);
   }
}
