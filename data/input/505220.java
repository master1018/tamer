public final class elementreplaceexistingattribute extends DOMTestCase {
   public elementreplaceexistingattribute(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Element testEmployee;
      Attr newAttribute;
      String name;
      Attr setAttr;
      doc = (Document) load("staff", true);
      elementList = doc.getElementsByTagName("address");
      testEmployee = (Element) elementList.item(2);
      newAttribute = doc.createAttribute("street");
      setAttr = testEmployee.setAttributeNode(newAttribute);
      name = testEmployee.getAttribute("street");
      assertEquals("elementReplaceExistingAttributeAssert", "", name);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(elementreplaceexistingattribute.class, args);
   }
}
