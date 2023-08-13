public final class importNode12 extends DOMTestCase {
   public importNode12(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      Node child;
      String childName;
      doc = (Document) load("staffNS", true);
      aNewDoc = (Document) load("staffNS", true);
      doc1Type = aNewDoc.getDoctype();
      entityList = doc1Type.getEntities();
      assertNotNull("entitiesNotNull", entityList);
      entity2 = (Entity) entityList.getNamedItem("ent4");
      entity1 = (Entity) doc.importNode(entity2, true);
      ownerDocument = entity1.getOwnerDocument();
      docType = ownerDocument.getDoctype();
      system = docType.getSystemId();
      assertURIEquals("systemId", null, null, null, "staffNS.dtd", null, null, null, null, system);
entityName = entity1.getNodeName();
      assertEquals("entityName", "ent4", entityName);
      child = entity1.getFirstChild();
      assertNotNull("notnull", child);
      childName = child.getNodeName();
      assertEquals("childName", "entElement1", childName);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(importNode12.class, args);
   }
}
