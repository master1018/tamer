public final class setAttributeNS09 extends DOMTestCase {
   public setAttributeNS09(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", true);
    }
   public void runTest() throws Throwable {
      String localName = "newAttr";
      String namespaceURI = "http:
      String qualifiedName = "emp:newAttr";
      Document doc;
      NodeList elementList;
      Node testAddr;
      Attr addrAttr;
      String resultAttr;
      String resultNamespaceURI;
      String resultLocalName;
      String resultPrefix;
      doc = (Document) load("staffNS", true);
      elementList = doc.getElementsByTagName("emp:address");
      testAddr = elementList.item(0);
      assertNotNull("empAddrNotNull", testAddr);
      ((Element) testAddr).setAttributeNS(namespaceURI, qualifiedName, "newValue");
      addrAttr = ((Element) testAddr).getAttributeNodeNS(namespaceURI, localName);
      resultAttr = ((Element) testAddr).getAttributeNS(namespaceURI, localName);
      assertEquals("attrValue", "newValue", resultAttr);
      resultNamespaceURI = addrAttr.getNamespaceURI();
      assertEquals("nsuri", "http:
      resultLocalName = addrAttr.getLocalName();
      assertEquals("lname", "newAttr", resultLocalName);
      resultPrefix = addrAttr.getPrefix();
      assertEquals("prefix", "emp", resultPrefix);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(setAttributeNS09.class, args);
   }
}
