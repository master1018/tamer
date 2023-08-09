public final class importNode09 extends DOMTestCase {
   public importNode09(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", true);
    preload(contentType, "staffNS", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      Document aNewDoc;
      DocumentType doc1Type;
      NamedNodeMap entityList;
      Entity entity2;
      Entity entity1;
      Document ownerDocument;
      DocumentType docType;
      String system;
      String entityName;
      String publicVal;
      String notationName;
      doc = (Document) load("staffNS", true);
      aNewDoc = (Document) load("staffNS", true);
      docType = aNewDoc.getDoctype();
      entityList = docType.getEntities();
      assertNotNull("entitiesNotNull", entityList);
      entity2 = (Entity) entityList.getNamedItem("ent6");
      entity1 = (Entity) doc.importNode(entity2, false);
      ownerDocument = entity1.getOwnerDocument();
      docType = ownerDocument.getDoctype();
      system = docType.getSystemId();
      assertURIEquals("dtdSystemId", null, null, null, "staffNS.dtd", null, null, null, null, system);
entityName = entity1.getNodeName();
      assertEquals("entityName", "ent6", entityName);
      publicVal = entity1.getPublicId();
      assertEquals("entityPublicId", "uri", publicVal);
      system = entity1.getSystemId();
      assertURIEquals("entitySystemId", null, null, null, "file", null, null, null, null, system);
notationName = entity1.getNotationName();
      assertEquals("notationName", "notation2", notationName);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(importNode09.class, args);
   }
}
