public final class getNamedItemNS02 extends DOMTestCase {
   public getNamedItemNS02(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", false);
    }
   public void runTest() throws Throwable {
      String namespaceURI = "http:
      String localName = "domest";
      Document doc;
      NodeList elementList;
      Node testEmployee;
      NamedNodeMap attributes;
      Attr newAttr;
      doc = (Document) load("staffNS", false);
      elementList = doc.getElementsByTagName("address");
      testEmployee = elementList.item(1);
      attributes = testEmployee.getAttributes();
      newAttr = (Attr) attributes.getNamedItemNS(namespaceURI, localName);
      assertNull("throw_Null", newAttr);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(getNamedItemNS02.class, args);
   }
}
