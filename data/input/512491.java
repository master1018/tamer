public final class documenttypegetnotations extends DOMTestCase {
   public documenttypegetnotations(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      DocumentType docType;
      NamedNodeMap notationList;
      Node notation;
      String notationName;
      java.util.Collection actual = new java.util.ArrayList();
      java.util.Collection expected = new java.util.ArrayList();
      expected.add("notation1");
      expected.add("notation2");
      doc = (Document) load("staff", false);
      docType = doc.getDoctype();
      assertNotNull("docTypeNotNull", docType);
      notationList = docType.getNotations();
      assertNotNull("notationsNotNull", notationList);
      for (int indexN1005B = 0; indexN1005B < notationList.getLength(); indexN1005B++) {
          notation = (Node) notationList.item(indexN1005B);
    notationName = notation.getNodeName();
      actual.add(notationName);
        }
      assertEquals("names", expected, actual);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(documenttypegetnotations.class, args);
   }
}
