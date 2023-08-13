public final class namednodemapgetnameditemns01 extends DOMTestCase {
   public namednodemapgetnameditemns01(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      org.w3c.domts.DocumentBuilderSetting[] settings = 
          new org.w3c.domts.DocumentBuilderSetting[] {
org.w3c.domts.DocumentBuilderSetting.namespaceAware
        };
        DOMTestDocumentBuilderFactory testFactory = factory.newInstance(settings);
        setFactory(testFactory);
    String contentType = getContentType();
    preload(contentType, "staffNS", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      DocumentType docType;
      NamedNodeMap entities;
      NamedNodeMap notations;
      Entity entity;
      Notation notation;
      String entityName;
      String notationName;
      String nullNS = null;
      doc = (Document) load("staffNS", false);
      docType = doc.getDoctype();
      entities = docType.getEntities();
      assertNotNull("entitiesNotNull", entities);
      notations = docType.getNotations();
      assertNotNull("notationsNotNull", notations);
      entity = (Entity) entities.getNamedItemNS(nullNS, "ent1");
      assertNull("entityNull", entity);
      notation = (Notation) notations.getNamedItemNS(nullNS, "notation1");
      assertNull("notationNull", notation);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(namednodemapgetnameditemns01.class, args);
   }
}
