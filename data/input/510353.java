public final class nodeentitysetnodevalue extends DOMTestCase {
   public nodeentitysetnodevalue(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      DocumentType docType;
      NamedNodeMap entities;
      Node entityNode;
      String entityValue;
      doc = (Document) load("staff", true);
      docType = doc.getDoctype();
      assertNotNull("docTypeNotNull", docType);
      entities = docType.getEntities();
      assertNotNull("entitiesNotNull", entities);
      entityNode = entities.getNamedItem("ent1");
      assertNotNull("ent1NotNull", entityNode);
      entityNode.setNodeValue("This should have no effect");
      entityValue = entityNode.getNodeValue();
      assertNull("nodeValueNull", entityValue);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodeentitysetnodevalue.class, args);
   }
}
