public final class setAttributeNodeNS03 extends DOMTestCase {
   public setAttributeNodeNS03(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", true);
    }
   public void runTest() throws Throwable {
      String namespaceURI = "http:
      String qualifiedName = "emp:newAttr";
      Document doc;
      NodeList elementList;
      Node testAddr;
      Attr newAttr;
      Attr newAddrAttr;
      doc = (Document) load("staffNS", true);
      elementList = doc.getElementsByTagName("emp:address");
      testAddr = elementList.item(0);
      assertNotNull("empAddrNotNull", testAddr);
      newAttr = doc.createAttributeNS(namespaceURI, qualifiedName);
      newAddrAttr = ((Element) testAddr).setAttributeNodeNS(newAttr);
      assertNull("throw_Null", newAddrAttr);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(setAttributeNodeNS03.class, args);
   }
}
