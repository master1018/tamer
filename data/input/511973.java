public final class hc_elementcreatenewattribute extends DOMTestCase {
   public hc_elementcreatenewattribute(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Element testAddress;
      Attr newAttribute;
      Attr oldAttr;
      Attr districtAttr;
      String attrVal;
      doc = (Document) load("hc_staff", true);
      elementList = doc.getElementsByTagName("acronym");
      testAddress = (Element) elementList.item(0);
      newAttribute = doc.createAttribute("lang");
      oldAttr = testAddress.setAttributeNode(newAttribute);
      assertNull("old_attr_doesnt_exist", oldAttr);
      districtAttr = testAddress.getAttributeNode("lang");
      assertNotNull("new_district_accessible", districtAttr);
      attrVal = testAddress.getAttribute("lang");
      assertEquals("attr_value", "", attrVal);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_elementcreatenewattribute.class, args);
   }
}
