public final class documenttypegetentitiestype extends DOMTestCase {
   public documenttypegetentitiestype(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      DocumentType docType;
      NamedNodeMap entityList;
      Node entity;
      int entityType;
      doc = (Document) load("staff", false);
      docType = doc.getDoctype();
      assertNotNull("docTypeNotNull", docType);
      entityList = docType.getEntities();
      assertNotNull("entitiesNotNull", entityList);
      for (int indexN10049 = 0; indexN10049 < entityList.getLength(); indexN10049++) {
          entity = (Node) entityList.item(indexN10049);
    entityType = (int) entity.getNodeType();
      assertEquals("documenttypeGetEntitiesTypeAssert", 6, entityType);
        }
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(documenttypegetentitiestype.class, args);
   }
}
