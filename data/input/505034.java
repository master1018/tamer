public final class hc_elementreplaceexistingattribute extends DOMTestCase {
   public hc_elementreplaceexistingattribute(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Element testEmployee;
      Attr newAttribute;
      String strong;
      Attr setAttr;
      doc = (Document) load("hc_staff", true);
      elementList = doc.getElementsByTagName("acronym");
      testEmployee = (Element) elementList.item(2);
      newAttribute = doc.createAttribute("class");
      setAttr = testEmployee.setAttributeNode(newAttribute);
      strong = testEmployee.getAttribute("class");
      assertEquals("replacedValue", "", strong);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_elementreplaceexistingattribute.class, args);
   }
}
