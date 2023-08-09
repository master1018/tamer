public final class setNamedItemNS02 extends DOMTestCase {
   public setNamedItemNS02(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", true);
    preload(contentType, "staffNS", true);
    }
   public void runTest() throws Throwable {
      String namespaceURI = "http:
      String qualifiedName = "dmstc:domestic";
      Document doc;
      Document anotherDoc;
      Node arg;
      NodeList elementList;
      Node testAddress;
      NamedNodeMap attributes;
      Node setNode;
      doc = (Document) load("staffNS", true);
      anotherDoc = (Document) load("staffNS", true);
      arg = anotherDoc.createAttributeNS(namespaceURI, qualifiedName);
      arg.setNodeValue("Maybe");
      elementList = doc.getElementsByTagName("address");
      testAddress = elementList.item(0);
      attributes = testAddress.getAttributes();
      {
         boolean success = false;
         try {
            setNode = attributes.setNamedItemNS(arg);
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
        DOMTestCase.doMain(setNamedItemNS02.class, args);
   }
}
