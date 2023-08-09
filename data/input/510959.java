public final class hc_elementgetattributenodenull extends DOMTestCase {
   public hc_elementgetattributenodenull(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Element testEmployee;
      Attr domesticAttr;
      doc = (Document) load("hc_staff", true);
      elementList = doc.getElementsByTagName("acronym");
      testEmployee = (Element) elementList.item(0);
      domesticAttr = testEmployee.getAttributeNode("invalidAttribute");
      assertNull("elementGetAttributeNodeNullAssert", domesticAttr);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_elementgetattributenodenull.class, args);
   }
}
