public final class getNamedItemNS03 extends DOMTestCase {
   public getNamedItemNS03(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      DocumentType docType;
      NamedNodeMap entities;
      Entity entity;
      String nullNS = null;
      doc = (Document) load("staffNS", false);
      docType = doc.getDoctype();
      entities = docType.getEntities();
      assertNotNull("entitiesNotNull", entities);
      entity = (Entity) entities.getNamedItemNS(nullNS, "ent1");
      assertNull("entityNull", entity);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(getNamedItemNS03.class, args);
   }
}
