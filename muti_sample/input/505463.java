public final class setAttributeNodeNS01 extends DOMTestCase {
   public setAttributeNodeNS01(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", true);
    }
   public void runTest() throws Throwable {
      String namespaceURI = "http:
      String qualifiedName = "emp:newAttr";
      Document doc;
      Element newElement;
      Attr newAttr;
      NodeList elementList;
      Node testAddr;
      Node appendedChild;
      Attr setAttr1;
      Attr setAttr2;
      doc = (Document) load("staffNS", true);
      elementList = doc.getElementsByTagName("emp:address");
      testAddr = elementList.item(0);
      assertNotNull("empAddrNotNull", testAddr);
      newElement = doc.createElement("newElement");
      appendedChild = testAddr.appendChild(newElement);
      newAttr = doc.createAttributeNS(namespaceURI, qualifiedName);
      setAttr1 = newElement.setAttributeNodeNS(newAttr);
      {
         boolean success = false;
         try {
            setAttr2 = ((Element) testAddr).setAttributeNodeNS(newAttr);
          } catch (DOMException ex) {
            success = (ex.code == DOMException.INUSE_ATTRIBUTE_ERR);
         }
         assertTrue("throw_INUSE_ATTRIBUTE_ERR", success);
      }
}
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(setAttributeNodeNS01.class, args);
   }
}
