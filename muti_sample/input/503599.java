public final class elementremoveattributenode extends DOMTestCase {
   public elementremoveattributenode(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Element testEmployee;
      Attr streetAttr;
      Attr removedAttr;
      String removedValue;
      doc = (Document) load("staff", true);
      elementList = doc.getElementsByTagName("address");
      testEmployee = (Element) elementList.item(2);
      streetAttr = testEmployee.getAttributeNode("street");
      removedAttr = testEmployee.removeAttributeNode(streetAttr);
      removedValue = removedAttr.getValue();
      assertEquals("elementRemoveAttributeNodeAssert", "No", removedValue);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(elementremoveattributenode.class, args);
   }
}
