public final class nodenotationnodename extends DOMTestCase {
   public nodenotationnodename(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      DocumentType docType;
      NamedNodeMap notations;
      Node notationNode;
      String notationName;
      doc = (Document) load("staff", false);
      docType = doc.getDoctype();
      assertNotNull("docTypeNotNull", docType);
      notations = docType.getNotations();
      assertNotNull("notationsNotNull", notations);
      notationNode = notations.getNamedItem("notation1");
      assertNotNull("notationNotNull", notationNode);
      notationName = notationNode.getNodeName();
      assertEquals("nodeName", "notation1", notationName);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodenotationnodename.class, args);
   }
}
