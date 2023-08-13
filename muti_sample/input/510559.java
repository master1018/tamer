public final class nodeentitynodeattributes extends DOMTestCase {
   public nodeentitynodeattributes(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      DocumentType docType;
      NamedNodeMap entities;
      Node entityNode;
      NamedNodeMap attrList;
      doc = (Document) load("staff", false);
      docType = doc.getDoctype();
      assertNotNull("docTypeNotNull", docType);
      entities = docType.getEntities();
      assertNotNull("entitiesNotNull", entities);
      entityNode = entities.getNamedItem("ent1");
      assertNotNull("ent1NotNull", entityNode);
      attrList = entityNode.getAttributes();
      assertNull("entityAttributesNull", attrList);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodeentitynodeattributes.class, args);
   }
}
