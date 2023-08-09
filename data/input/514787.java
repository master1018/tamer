public final class nodeentitynodetype extends DOMTestCase {
   public nodeentitynodetype(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      DocumentType docType;
      NamedNodeMap entities;
      Node entityNode;
      int nodeType;
      doc = (Document) load("staff", false);
      docType = doc.getDoctype();
      assertNotNull("docTypeNotNull", docType);
      entities = docType.getEntities();
      assertNotNull("entitiesNotNull", entities);
      entityNode = entities.getNamedItem("ent1");
      assertNotNull("ent1NotNull", entityNode);
      nodeType = (int) entityNode.getNodeType();
      assertEquals("entityNodeType", 6, nodeType);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodeentitynodetype.class, args);
   }
}
