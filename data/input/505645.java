public final class getAttributeNS03 extends DOMTestCase {
   public getAttributeNS03(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", true);
    }
   public void runTest() throws Throwable {
      String namespaceURI = "http:
      String localName = "domestic";
      Document doc;
      NodeList elementList;
      Element testAddr;
      String attrValue;
      doc = (Document) load("staffNS", true);
      elementList = doc.getElementsByTagName("emp:address");
      testAddr = (Element) elementList.item(0);
      assertNotNull("empAddrNotNull", testAddr);
      testAddr.removeAttributeNS(namespaceURI, localName);
      attrValue = testAddr.getAttributeNS(namespaceURI, localName);
      assertEquals("throw_Equals", "", attrValue);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(getAttributeNS03.class, args);
   }
}
