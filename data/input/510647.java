public final class entitygetentityname extends DOMTestCase {
   public entitygetentityname(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      DocumentType docType;
      NamedNodeMap entityList;
      Entity entityNode;
      String entityName;
      doc = (Document) load("staff", false);
      docType = doc.getDoctype();
      assertNotNull("docTypeNotNull", docType);
      entityList = docType.getEntities();
      assertNotNull("entitiesNotNull", entityList);
      entityNode = (Entity) entityList.getNamedItem("ent1");
      entityName = entityNode.getNodeName();
      assertEquals("entityGetEntityNameAssert", "ent1", entityName);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(entitygetentityname.class, args);
   }
}
