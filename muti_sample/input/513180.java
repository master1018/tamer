public final class entitygetpublicid extends DOMTestCase {
   public entitygetpublicid(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      DocumentType docType;
      NamedNodeMap entityList;
      Entity entityNode;
      String publicId;
      String systemId;
      String notation;
      doc = (Document) load("staff", false);
      docType = doc.getDoctype();
      assertNotNull("docTypeNotNull", docType);
      entityList = docType.getEntities();
      assertNotNull("entitiesNotNull", entityList);
      entityNode = (Entity) entityList.getNamedItem("ent5");
      publicId = entityNode.getPublicId();
      assertEquals("publicId", "entityURI", publicId);
      systemId = entityNode.getSystemId();
      assertURIEquals("systemId", null, null, null, "entityFile", null, null, null, null, systemId);
notation = entityNode.getNotationName();
      assertEquals("notation", "notation1", notation);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(entitygetpublicid.class, args);
   }
}
