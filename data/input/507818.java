public final class nodeentitynodename extends DOMTestCase {
   public nodeentitynodename(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      DocumentType docType;
      NamedNodeMap entities;
      Node entityNode;
      String entityName;
      doc = (Document) load("staff", false);
      docType = doc.getDoctype();
      assertNotNull("docTypeNotNull", docType);
      entities = docType.getEntities();
      assertNotNull("entitiesNotNull", entities);
      entityNode = entities.getNamedItem("ent1");
      assertNotNull("entityNodeNotNull", entityNode);
      entityName = entityNode.getNodeName();
      assertEquals("entityNodeName", "ent1", entityName);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodeentitynodename.class, args);
   }
}
