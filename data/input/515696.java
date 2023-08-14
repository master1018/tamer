public final class namednodemapremovenameditemns05 extends DOMTestCase {
   public namednodemapremovenameditemns05(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      Node removedNode;
      String nullNS = null;
      doc = (Document) load("staffNS", true);
      docType = doc.getDoctype();
      entities = docType.getEntities();
      assertNotNull("entitiesNotNull", entities);
      notations = docType.getNotations();
      assertNotNull("notationsNotNull", notations);
      try {
      removedNode = entities.removeNamedItemNS(nullNS, "ent1");
      fail("entity_throw_DOMException");
      } catch (DOMException ex) {
           switch (ex.code) {
      case 8 : 
       break;
      case 7 : 
       break;
          default:
          throw ex;
          }
      } 
      try {
      removedNode = notations.removeNamedItemNS(nullNS, "notation1");
      fail("notation_throw_DOMException");
      } catch (DOMException ex) {
           switch (ex.code) {
      case 8 : 
       break;
      case 7 : 
       break;
          default:
          throw ex;
          }
      } 
}
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(namednodemapremovenameditemns05.class, args);
   }
}
