public final class documentimportnode19 extends DOMTestCase {
   public documentimportnode19(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      DocumentType docTypeNull = null;
      Document docImp;
      DOMImplementation domImpl;
      DocumentType docType;
      NamedNodeMap nodeMap;
      Entity entity2;
      Entity entity6;
      Entity entityImp2;
      Entity entityImp6;
      String nodeName;
      String systemId;
      String notationName;
      String nodeNameImp;
      String systemIdImp;
      String notationNameImp;
      doc = (Document) load("staffNS", true);
      domImpl = doc.getImplementation();
      docType = doc.getDoctype();
      docImp = domImpl.createDocument("http:
      nodeMap = docType.getEntities();
      assertNotNull("entitiesNotNull", nodeMap);
      entity2 = (Entity) nodeMap.getNamedItem("ent2");
      entity6 = (Entity) nodeMap.getNamedItem("ent6");
      entityImp2 = (Entity) docImp.importNode(entity2, false);
      entityImp6 = (Entity) docImp.importNode(entity6, true);
      nodeName = entity2.getNodeName();
      nodeNameImp = entityImp2.getNodeName();
      assertEquals("documentimportnode19_Ent2NodeName", nodeName, nodeNameImp);
      nodeName = entity6.getNodeName();
      nodeNameImp = entityImp6.getNodeName();
      assertEquals("documentimportnode19_Ent6NodeName", nodeName, nodeNameImp);
      systemId = entity2.getSystemId();
      systemIdImp = entityImp2.getSystemId();
      assertEquals("documentimportnode19_Ent2SystemId", systemId, systemIdImp);
      systemId = entity6.getSystemId();
      systemIdImp = entityImp6.getSystemId();
      assertEquals("documentimportnode19_Ent6SystemId", systemId, systemIdImp);
      notationName = entity2.getNotationName();
      notationNameImp = entityImp2.getNotationName();
      assertEquals("documentimportnode19_Ent2NotationName", notationName, notationNameImp);
      notationName = entity6.getNotationName();
      notationNameImp = entityImp6.getNotationName();
      assertEquals("documentimportnode19_Ent6NotationName", notationName, notationNameImp);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(documentimportnode19.class, args);
   }
}
