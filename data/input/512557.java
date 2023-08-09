public final class setAttributeNodeNS05 extends DOMTestCase {
   public setAttributeNodeNS05(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", true);
    preload(contentType, "staffNS", true);
    }
   public void runTest() throws Throwable {
      String namespaceURI = "http:
      String qualifiedName = "emp:newAttr";
      Document doc1;
      Document doc2;
      Attr newAttr;
      NodeList elementList;
      Node testAddr;
      Attr setAttr1;
      doc1 = (Document) load("staffNS", true);
      doc2 = (Document) load("staffNS", true);
      newAttr = doc2.createAttributeNS(namespaceURI, qualifiedName);
      elementList = doc1.getElementsByTagName("emp:address");
      testAddr = elementList.item(0);
      {
         boolean success = false;
         try {
            setAttr1 = ((Element) testAddr).setAttributeNodeNS(newAttr);
          } catch (DOMException ex) {
            success = (ex.code == DOMException.WRONG_DOCUMENT_ERR);
         }
         assertTrue("throw_WRONG_DOCUMENT_ERR", success);
      }
}
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(setAttributeNodeNS05.class, args);
   }
}
