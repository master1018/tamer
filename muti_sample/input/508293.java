public final class notationgetsystemidnull extends DOMTestCase {
   public notationgetsystemidnull(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      doc = (Document) load("staff", false);
      docType = doc.getDoctype();
      assertNotNull("docTypeNotNull", docType);
      notations = docType.getNotations();
      assertNotNull("notationsNotNull", notations);
      notationNode = (Notation) notations.getNamedItem("notation1");
      systemId = notationNode.getSystemId();
      assertNull("systemId", systemId);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(notationgetsystemidnull.class, args);
   }
}
