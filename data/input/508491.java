public final class notationgetsystemid extends DOMTestCase {
   public notationgetsystemid(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      DocumentType docType;
      NamedNodeMap notations;
      Notation notationNode;
      String systemId;
      int index;
      doc = (Document) load("staff", false);
      docType = doc.getDoctype();
      assertNotNull("docTypeNotNull", docType);
      notations = docType.getNotations();
      assertNotNull("notationsNotNull", notations);
      notationNode = (Notation) notations.getNamedItem("notation2");
      systemId = notationNode.getSystemId();
      assertURIEquals("uriEquals", null, null, null, "notation2File", null, null, null, null, systemId);
}
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(notationgetsystemid.class, args);
   }
}
