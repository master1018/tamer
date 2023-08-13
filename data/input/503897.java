public final class documentimportnode20 extends DOMTestCase {
   public documentimportnode20(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      org.w3c.domts.DocumentBuilderSetting[] settings = 
          new org.w3c.domts.DocumentBuilderSetting[] {
org.w3c.domts.DocumentBuilderSetting.namespaceAware,
org.w3c.domts.DocumentBuilderSetting.notExpandEntityReferences
        };
        DOMTestDocumentBuilderFactory testFactory = factory.newInstance(settings);
        setFactory(testFactory);
    String contentType = getContentType();
    preload(contentType, "staffNS", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      Document docImp;
      DOMImplementation domImpl;
      DocumentType docType;
      DocumentType docTypeNull = null;
      NamedNodeMap nodeMap;
      Entity entity4;
      Entity entityImp4;
      Element element;
      CharacterData cdata;
      ProcessingInstruction pi;
      NodeList childList;
      NodeList elemchildList;
      String ent4Name;
      String ent4ImpName;
      String cdataVal;
      String piTargetVal;
      String piDataVal;
      doc = (Document) load("staffNS", true);
      domImpl = doc.getImplementation();
      docType = doc.getDoctype();
      docImp = domImpl.createDocument("http:
      nodeMap = docType.getEntities();
      entity4 = (Entity) nodeMap.getNamedItem("ent4");
      entityImp4 = (Entity) docImp.importNode(entity4, true);
      childList = entityImp4.getChildNodes();
      element = (Element) childList.item(0);
      elemchildList = element.getChildNodes();
      cdata = (CharacterData) elemchildList.item(0);
      pi = (ProcessingInstruction) childList.item(1);
      ent4Name = entity4.getNodeName();
      ent4ImpName = entityImp4.getNodeName();
      cdataVal = cdata.getData();
      piTargetVal = pi.getTarget();
      piDataVal = pi.getData();
      assertEquals("documentimportnode20_Ent4NodeName", ent4Name, ent4ImpName);
      assertEquals("documentimportnode20_Cdata", "Element data", cdataVal);
      assertEquals("documentimportnode20_PITarget", "PItarget", piTargetVal);
      assertEquals("documentimportnode20_PIData", "PIdata", piDataVal);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(documentimportnode20.class, args);
   }
}
