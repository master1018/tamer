public final class elementreplaceexistingattributegevalue extends DOMTestCase {
   public elementreplaceexistingattributegevalue(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Element testEmployee;
      Attr newAttribute;
      Attr streetAttr;
      String value;
      doc = (Document) load("staff", true);
      elementList = doc.getElementsByTagName("address");
      testEmployee = (Element) elementList.item(2);
      newAttribute = doc.createAttribute("street");
      streetAttr = testEmployee.setAttributeNode(newAttribute);
      value = streetAttr.getValue();
      assertEquals("streetNo", "No", value);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(elementreplaceexistingattributegevalue.class, args);
   }
}
