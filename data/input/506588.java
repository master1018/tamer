public final class getAttributeNS02 extends DOMTestCase {
   public getAttributeNS02(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", true);
    }
   public void runTest() throws Throwable {
      String namespaceURI = "http:
      String localName = "district";
      String qualifiedName = "emp:district";
      Document doc;
      Attr newAttribute;
      NodeList elementList;
      Element testAddr;
      Attr districtAttr;
      String attrValue;
      doc = (Document) load("staffNS", true);
      newAttribute = doc.createAttributeNS(namespaceURI, qualifiedName);
      elementList = doc.getElementsByTagName("emp:address");
      testAddr = (Element) elementList.item(0);
      assertNotNull("empAddrNotNull", testAddr);
      districtAttr = testAddr.setAttributeNodeNS(newAttribute);
      elementList = doc.getElementsByTagName("emp:address");
      testAddr = (Element) elementList.item(0);
      attrValue = testAddr.getAttributeNS(namespaceURI, localName);
      assertEquals("throw_Equals", "", attrValue);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(getAttributeNS02.class, args);
   }
}
