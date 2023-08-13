public final class elementsetattributenodenull extends DOMTestCase {
   public elementsetattributenodenull(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Element testEmployee;
      Attr newAttribute;
      Attr districtAttr;
      doc = (Document) load("staff", true);
      elementList = doc.getElementsByTagName("address");
      testEmployee = (Element) elementList.item(2);
      newAttribute = doc.createAttribute("district");
      districtAttr = testEmployee.setAttributeNode(newAttribute);
      assertNull("elementSetAttributeNodeNullAssert", districtAttr);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(elementsetattributenodenull.class, args);
   }
}
