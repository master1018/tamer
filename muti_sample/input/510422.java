public final class notationgetpublicidnull extends DOMTestCase {
   public notationgetpublicidnull(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      DocumentType docType;
      NamedNodeMap notations;
      Notation notationNode;
      String publicId;
      doc = (Document) load("staff", false);
      docType = doc.getDoctype();
      assertNotNull("docTypeNotNull", docType);
      notations = docType.getNotations();
      assertNotNull("notationsNotNull", notations);
      notationNode = (Notation) notations.getNamedItem("notation2");
      publicId = notationNode.getPublicId();
      assertNull("publicId", publicId);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(notationgetpublicidnull.class, args);
   }
}
