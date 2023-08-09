public final class namednodemapsetnameditemns05 extends DOMTestCase {
   public namednodemapsetnameditemns05(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      org.w3c.domts.DocumentBuilderSetting[] settings = 
          new org.w3c.domts.DocumentBuilderSetting[] {
org.w3c.domts.DocumentBuilderSetting.namespaceAware
        };
        DOMTestDocumentBuilderFactory testFactory = factory.newInstance(settings);
        setFactory(testFactory);
    String contentType = getContentType();
    preload(contentType, "staffNS", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      DocumentType docType;
      NamedNodeMap entities;
      NamedNodeMap notations;
      Entity entity;
      Notation notation;
      Node newNode;
      String nullNS = null;
      doc = (Document) load("staffNS", true);
      docType = doc.getDoctype();
      entities = docType.getEntities();
      assertNotNull("entitiesNotNull", entities);
      notations = docType.getNotations();
      assertNotNull("notationsNotNull", notations);
      entity = (Entity) entities.getNamedItem("ent1");
      notation = (Notation) notations.getNamedItem("notation1");
      {
         boolean success = false;
         try {
            newNode = entities.setNamedItemNS(entity);
          } catch (DOMException ex) {
            success = (ex.code == DOMException.NO_MODIFICATION_ALLOWED_ERR);
         }
         assertTrue("throw_NO_MODIFICATION_ALLOWED_ERR_entities", success);
      }
      {
         boolean success = false;
         try {
            newNode = notations.setNamedItemNS(notation);
          } catch (DOMException ex) {
            success = (ex.code == DOMException.NO_MODIFICATION_ALLOWED_ERR);
         }
         assertTrue("throw_NO_MODIFICATION_ALLOWED_ERR_notations", success);
      }
}
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(namednodemapsetnameditemns05.class, args);
   }
}
