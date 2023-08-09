public final class namednodemapsetnameditemns10 extends DOMTestCase {
   public namednodemapsetnameditemns10(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      NamedNodeMap attributes;
      Entity entity;
      Notation notation;
      Element element;
      NodeList elementList;
      Node newNode;
      doc = (Document) load("staffNS", true);
      docType = doc.getDoctype();
      entities = docType.getEntities();
      assertNotNull("entitiesNotNull", entities);
      entity = (Entity) entities.getNamedItem("ent1");
      elementList = doc.getElementsByTagNameNS("http:
      element = (Element) elementList.item(0);
      attributes = element.getAttributes();
      {
         boolean success = false;
         try {
            newNode = attributes.setNamedItemNS(entity);
          } catch (DOMException ex) {
            success = (ex.code == DOMException.HIERARCHY_REQUEST_ERR);
         }
         assertTrue("throw_HIERARCHY_REQUEST_ERR", success);
      }
}
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(namednodemapsetnameditemns10.class, args);
   }
}
