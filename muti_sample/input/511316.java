public final class nodenotationnodetype extends DOMTestCase {
   public nodenotationnodetype(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      DocumentType docType;
      NamedNodeMap notations;
      Notation notationNode;
      int nodeType;
      doc = (Document) load("staff", false);
      docType = doc.getDoctype();
      assertNotNull("docTypeNotNull", docType);
      notations = docType.getNotations();
      assertNotNull("notationsNotNull", notations);
      notationNode = (Notation) notations.getNamedItem("notation1");
      assertNotNull("notationNotNull", notationNode);
      nodeType = (int) notationNode.getNodeType();
      assertEquals("nodeNotationNodeTypeAssert1", 12, nodeType);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodenotationnodetype.class, args);
   }
}
