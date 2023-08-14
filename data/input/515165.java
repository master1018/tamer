public final class documentimportnode22 extends DOMTestCase {
   public documentimportnode22(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      Notation notation1;
      Notation notation2;
      Notation notationImp1;
      Notation notationImp2;
      Notation notationImpNew1;
      Notation notationImpNew2;
      String publicId1;
      String publicId1Imp;
      String publicId1NewImp;
      String publicId2Imp;
      String publicId2NewImp;
      String systemId1Imp;
      String systemId1NewImp;
      String systemId2;
      String systemId2Imp;
      String systemId2NewImp;
      doc = (Document) load("staffNS", true);
      domImpl = doc.getImplementation();
      docType = doc.getDoctype();
      docImp = domImpl.createDocument("http:
      nodeMap = docType.getNotations();
      assertNotNull("notationsNotNull", nodeMap);
      notation1 = (Notation) nodeMap.getNamedItem("notation1");
      notation2 = (Notation) nodeMap.getNamedItem("notation2");
      notationImp1 = (Notation) doc.importNode(notation1, true);
      notationImp2 = (Notation) doc.importNode(notation2, false);
      notationImpNew1 = (Notation) docImp.importNode(notation1, false);
      notationImpNew2 = (Notation) docImp.importNode(notation2, true);
      publicId1 = notation1.getPublicId();
      publicId1Imp = notation1.getPublicId();
      publicId1NewImp = notation1.getPublicId();
      systemId1Imp = notation1.getSystemId();
      systemId1NewImp = notation1.getSystemId();
      publicId2Imp = notation2.getPublicId();
      publicId2NewImp = notation2.getPublicId();
      systemId2 = notation2.getSystemId();
      systemId2Imp = notation2.getSystemId();
      systemId2NewImp = notation2.getSystemId();
      assertEquals("documentimportnode22_N1PID", publicId1, publicId1Imp);
      assertEquals("documentimportnode22_N1NPID", publicId1, publicId1NewImp);
      assertNull("documentimportnode22_N1SID", systemId1Imp);
      assertNull("documentimportnode22_N1NSID", systemId1NewImp);
      assertEquals("documentimportnode22_N2SID", systemId2, systemId2Imp);
      assertEquals("documentimportnode22_N2NSID", systemId2, systemId2NewImp);
      assertNull("documentimportnode22_N2PID", publicId2Imp);
      assertNull("documentimportnode22_N2NPID", publicId2Imp);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(documentimportnode22.class, args);
   }
}
