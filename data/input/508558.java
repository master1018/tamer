public final class hc_elementgetelementempty extends DOMTestCase {
   public hc_elementgetelementempty(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      Attr newAttribute;
      NodeList elementList;
      Element testEmployee;
      Attr domesticAttr;
      String attrValue;
      doc = (Document) load("hc_staff", true);
      newAttribute = doc.createAttribute("lang");
      elementList = doc.getElementsByTagName("acronym");
      testEmployee = (Element) elementList.item(3);
      domesticAttr = testEmployee.setAttributeNode(newAttribute);
      attrValue = testEmployee.getAttribute("lang");
      assertEquals("elementGetElementEmptyAssert", "", attrValue);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_elementgetelementempty.class, args);
   }
}
