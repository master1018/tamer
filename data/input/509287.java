public final class hc_elementreplaceattributewithself extends DOMTestCase {
   public hc_elementreplaceattributewithself(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Element testEmployee;
      Attr streetAttr;
      Attr replacedAttr;
      String value;
      doc = (Document) load("hc_staff", true);
      elementList = doc.getElementsByTagName("acronym");
      testEmployee = (Element) elementList.item(2);
      streetAttr = testEmployee.getAttributeNode("class");
      replacedAttr = testEmployee.setAttributeNode(streetAttr);
      assertSame("replacedAttr", streetAttr, replacedAttr);
}
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_elementreplaceattributewithself.class, args);
   }
}
