public final class setAttributeNS05 extends DOMTestCase {
   public setAttributeNS05(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      doc = (Document) load("staffNS", true);
      elementList = doc.getElementsByTagName("emp:address");
      testAddr = elementList.item(0);
      assertNotNull("empAddrNotNull", testAddr);
      ((Element) testAddr).setAttributeNS(namespaceURI, qualifiedName, "<newValue>");
      resultAttr = ((Element) testAddr).getAttributeNS(namespaceURI, localName);
      assertEquals("throw_Equals", "<newValue>", resultAttr);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(setAttributeNS05.class, args);
   }
}
