public final class getNamedItemNS04 extends DOMTestCase {
   public getNamedItemNS04(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      DocumentType docType;
      NamedNodeMap notations;
      Notation notation;
      String nullNS = null;
      doc = (Document) load("staffNS", false);
      docType = doc.getDoctype();
      notations = docType.getNotations();
      assertNotNull("notationsNotNull", notations);
      notation = (Notation) notations.getNamedItemNS(nullNS, "notation1");
      assertNull("notationNull", notation);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(getNamedItemNS04.class, args);
   }
}
