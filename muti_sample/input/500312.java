public final class hc_elementreplaceexistingattributegevalue extends DOMTestCase {
   public hc_elementreplaceexistingattributegevalue(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Element testEmployee;
      Attr newAttribute;
      Attr streetAttr;
      String value;
      doc = (Document) load("hc_staff", true);
      elementList = doc.getElementsByTagName("acronym");
      testEmployee = (Element) elementList.item(2);
      newAttribute = doc.createAttribute("class");
      streetAttr = testEmployee.setAttributeNode(newAttribute);
      assertNotNull("previousAttrNotNull", streetAttr);
      value = streetAttr.getValue();
      assertEquals("previousAttrValue", "No", value);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_elementreplaceexistingattributegevalue.class, args);
   }
}
