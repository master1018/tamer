public final class elementcreatenewattribute extends DOMTestCase {
   public elementcreatenewattribute(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Element testAddress;
      Attr newAttribute;
      Attr oldAttr;
      Attr districtAttr;
      String attrVal;
      doc = (Document) load("staff", true);
      elementList = doc.getElementsByTagName("address");
      testAddress = (Element) elementList.item(0);
      newAttribute = doc.createAttribute("district");
      oldAttr = testAddress.setAttributeNode(newAttribute);
      assertNull("old_attr_doesnt_exist", oldAttr);
      districtAttr = testAddress.getAttributeNode("district");
      assertNotNull("new_district_accessible", districtAttr);
      attrVal = testAddress.getAttribute("district");
      assertEquals("attr_value", "", attrVal);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(elementcreatenewattribute.class, args);
   }
}
