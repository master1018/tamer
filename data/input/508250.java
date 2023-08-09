public final class hc_elementremoveattributenode extends DOMTestCase {
   public hc_elementremoveattributenode(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Element testEmployee;
      Attr streetAttr;
      Attr removedAttr;
      String removedValue;
      doc = (Document) load("hc_staff", true);
      elementList = doc.getElementsByTagName("acronym");
      testEmployee = (Element) elementList.item(2);
      streetAttr = testEmployee.getAttributeNode("class");
      removedAttr = testEmployee.removeAttributeNode(streetAttr);
      assertNotNull("removedAttrNotNull", removedAttr);
      removedValue = removedAttr.getValue();
      assertEquals("elementRemoveAttributeNodeAssert", "No", removedValue);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_elementremoveattributenode.class, args);
   }
}
