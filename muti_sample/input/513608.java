public final class elementwrongdocumenterr extends DOMTestCase {
   public elementwrongdocumenterr(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc1;
      Document doc2;
      Attr newAttribute;
      NodeList addressElementList;
      Element testAddress;
      Attr attrAddress;
      doc1 = (Document) load("staff", true);
      doc2 = (Document) load("staff", false);
      newAttribute = doc2.createAttribute("newAttribute");
      addressElementList = doc1.getElementsByTagName("address");
      testAddress = (Element) addressElementList.item(4);
      {
         boolean success = false;
         try {
            attrAddress = testAddress.setAttributeNode(newAttribute);
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
        DOMTestCase.doMain(elementwrongdocumenterr.class, args);
   }
}
