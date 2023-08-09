public final class getAttributeNodeNS01 extends DOMTestCase {
   public getAttributeNodeNS01(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", false);
    }
   public void runTest() throws Throwable {
      String namespaceURI = "http:
      String localName = "invalidlocalname";
      Document doc;
      NodeList elementList;
      Element testAddr;
      Attr attribute;
      doc = (Document) load("staffNS", false);
      elementList = doc.getElementsByTagName("emp:address");
      testAddr = (Element) elementList.item(0);
      assertNotNull("empAddrNotNull", testAddr);
      attribute = testAddr.getAttributeNodeNS(namespaceURI, localName);
      assertNull("throw_Null", attribute);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(getAttributeNodeNS01.class, args);
   }
}
