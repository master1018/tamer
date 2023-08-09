public final class getAttributeNS04 extends DOMTestCase {
   public getAttributeNS04(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", true);
    }
   public void runTest() throws Throwable {
      String namespaceURI = "http:
      String localName = "blank";
      String qualifiedName = "emp:blank";
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
      testAddr.setAttributeNS(namespaceURI, qualifiedName, "NewValue");
      attrValue = testAddr.getAttributeNS(namespaceURI, localName);
      assertEquals("throw_Equals", "NewValue", attrValue);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(getAttributeNS04.class, args);
   }
}
