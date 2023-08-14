public final class namednodemapwrongdocumenterr extends DOMTestCase {
   public namednodemapwrongdocumenterr(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document doc1;
      Document doc2;
      NodeList elementList;
      Node testAddress;
      NamedNodeMap attributes;
      Node newAttribute;
      Node setNode;
      doc1 = (Document) load("staff", true);
      doc2 = (Document) load("staff", true);
      elementList = doc1.getElementsByTagName("address");
      testAddress = elementList.item(2);
      newAttribute = doc2.createAttribute("newAttribute");
      attributes = testAddress.getAttributes();
      {
         boolean success = false;
         try {
            setNode = attributes.setNamedItem(newAttribute);
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
        DOMTestCase.doMain(namednodemapwrongdocumenterr.class, args);
   }
}
