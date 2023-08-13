public final class hc_elementaddnewattribute extends DOMTestCase {
   public hc_elementaddnewattribute(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      testEmployee = (Element) elementList.item(4);
      testEmployee.setAttribute("lang", "EN-us");
      attrValue = testEmployee.getAttribute("lang");
      assertEquals("attrValue", "EN-us", attrValue);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_elementaddnewattribute.class, args);
   }
}
