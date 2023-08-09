public final class hc_elementremoveattributeaftercreate extends DOMTestCase {
   public hc_elementremoveattributeaftercreate(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Element testEmployee;
      Attr newAttribute;
      NamedNodeMap attributes;
      Attr districtAttr;
      doc = (Document) load("hc_staff", true);
      elementList = doc.getElementsByTagName("acronym");
      testEmployee = (Element) elementList.item(2);
      newAttribute = doc.createAttribute("lang");
      districtAttr = testEmployee.setAttributeNode(newAttribute);
      districtAttr = testEmployee.removeAttributeNode(newAttribute);
      attributes = testEmployee.getAttributes();
      districtAttr = (Attr) attributes.getNamedItem("lang");
      assertNull("removed_item_null", districtAttr);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_elementremoveattributeaftercreate.class, args);
   }
}
