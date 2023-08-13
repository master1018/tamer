public final class nodeentitynodevalue extends DOMTestCase {
   public nodeentitynodevalue(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      DocumentType docType;
      NamedNodeMap entities;
      Node entityNode;
      String entityValue;
      doc = (Document) load("staff", false);
      docType = doc.getDoctype();
      assertNotNull("docTypeNotNull", docType);
      entities = docType.getEntities();
      assertNotNull("entitiesNotNull", entities);
      entityNode = entities.getNamedItem("ent1");
      assertNotNull("ent1NotNull", entityNode);
      entityValue = entityNode.getNodeValue();
      assertNull("entityNodeValue", entityValue);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodeentitynodevalue.class, args);
   }
}
