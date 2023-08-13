public final class hc_elementchangeattributevalue extends DOMTestCase {
   public hc_elementchangeattributevalue(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Element testEmployee;
      String attrValue;
      doc = (Document) load("hc_staff", true);
      elementList = doc.getElementsByTagName("acronym");
      testEmployee = (Element) elementList.item(3);
      testEmployee.setAttribute("class", "Neither");
      attrValue = testEmployee.getAttribute("class");
      assertEquals("elementChangeAttributeValueAssert", "Neither", attrValue);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_elementchangeattributevalue.class, args);
   }
}
