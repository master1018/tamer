public final class entitygetpublicidnull extends DOMTestCase {
   public entitygetpublicidnull(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      doc = (Document) load("staff", false);
      docType = doc.getDoctype();
      assertNotNull("docTypeNotNull", docType);
      entityList = docType.getEntities();
      assertNotNull("entitiesNotNull", entityList);
      entityNode = (Entity) entityList.getNamedItem("ent1");
      publicId = entityNode.getPublicId();
      assertNull("entityGetPublicIdNullAssert", publicId);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(entitygetpublicidnull.class, args);
   }
}
