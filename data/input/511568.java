public final class removeNamedItemNS02 extends DOMTestCase {
   public removeNamedItemNS02(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", true);
    }
   public void runTest() throws Throwable {
      String namespaceURI = "http:
      String localName = "domest";
      Document doc;
      NodeList elementList;
      Node testAddress;
      NamedNodeMap attributes;
      Node removedNode;
      doc = (Document) load("staffNS", true);
      elementList = doc.getElementsByTagName("address");
      testAddress = elementList.item(1);
      attributes = testAddress.getAttributes();
      {
         boolean success = false;
         try {
            removedNode = attributes.removeNamedItemNS(namespaceURI, localName);
          } catch (DOMException ex) {
            success = (ex.code == DOMException.NOT_FOUND_ERR);
         }
         assertTrue("throw_NOT_FOUND_ERR", success);
      }
}
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(removeNamedItemNS02.class, args);
   }
}
