public final class elementgetattributenode extends DOMTestCase {
   public elementgetattributenode(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Element testEmployee;
      Attr domesticAttr;
      String name;
      doc = (Document) load("staff", true);
      elementList = doc.getElementsByTagName("address");
      testEmployee = (Element) elementList.item(0);
      domesticAttr = testEmployee.getAttributeNode("domestic");
      name = domesticAttr.getNodeName();
      assertEquals("elementGetAttributeNodeAssert", "domestic", name);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(elementgetattributenode.class, args);
   }
}
