public final class elementreplaceattributewithself extends DOMTestCase {
   public elementreplaceattributewithself(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Element testEmployee;
      Attr streetAttr;
      Attr replacedAttr;
      doc = (Document) load("staff", true);
      elementList = doc.getElementsByTagName("address");
      testEmployee = (Element) elementList.item(2);
      streetAttr = testEmployee.getAttributeNode("street");
      replacedAttr = testEmployee.setAttributeNode(streetAttr);
      assertSame("replacedAttr", streetAttr, replacedAttr);
}
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(elementreplaceattributewithself.class, args);
   }
}
