public final class documenttypegetnotationstype extends DOMTestCase {
   public documenttypegetnotationstype(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      DocumentType docType;
      NamedNodeMap notationList;
      Node notation;
      int notationType;
      doc = (Document) load("staff", false);
      docType = doc.getDoctype();
      assertNotNull("docTypeNotNull", docType);
      notationList = docType.getNotations();
      assertNotNull("notationsNotNull", notationList);
      for (int indexN10049 = 0; indexN10049 < notationList.getLength(); indexN10049++) {
          notation = (Node) notationList.item(indexN10049);
    notationType = (int) notation.getNodeType();
      assertEquals("documenttypeGetNotationsTypeAssert", 12, notationType);
        }
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(documenttypegetnotationstype.class, args);
   }
}
