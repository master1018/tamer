public final class nodenotationnodeattributes extends DOMTestCase {
   public nodenotationnodeattributes(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      DocumentType docType;
      NamedNodeMap notations;
      Node notationNode;
      NamedNodeMap attrList;
      doc = (Document) load("staff", false);
      docType = doc.getDoctype();
      assertNotNull("docTypeNotNull", docType);
      notations = docType.getNotations();
      assertNotNull("notationsNotNull", notations);
      notationNode = notations.getNamedItem("notation1");
      assertNotNull("notationNotNull", notationNode);
      attrList = notationNode.getAttributes();
      assertNull("nodeNotationNodeAttributesAssert1", attrList);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodenotationnodeattributes.class, args);
   }
}
