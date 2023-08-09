public final class hc_namednodemapwrongdocumenterr extends DOMTestCase {
   public hc_namednodemapwrongdocumenterr(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", true);
    preload(contentType, "hc_staff", true);
    }
   public void runTest() throws Throwable {
      Document doc1;
      Document doc2;
      NodeList elementList;
      Node testAddress;
      NamedNodeMap attributes;
      Node newAttribute;
      String strong;
      Node setNode;
      doc1 = (Document) load("hc_staff", true);
      doc2 = (Document) load("hc_staff", true);
      elementList = doc1.getElementsByTagName("acronym");
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
        DOMTestCase.doMain(hc_namednodemapwrongdocumenterr.class, args);
   }
}
