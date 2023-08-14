public final class elementgetelementempty extends DOMTestCase {
   public elementgetelementempty(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      Attr newAttribute;
      NodeList elementList;
      Element testEmployee;
      Attr domesticAttr;
      String attrValue;
      doc = (Document) load("staff", true);
      newAttribute = doc.createAttribute("district");
      elementList = doc.getElementsByTagName("address");
      testEmployee = (Element) elementList.item(3);
      domesticAttr = testEmployee.setAttributeNode(newAttribute);
      attrValue = testEmployee.getAttribute("district");
      assertEquals("elementGetElementEmptyAssert", "", attrValue);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(elementgetelementempty.class, args);
   }
}
